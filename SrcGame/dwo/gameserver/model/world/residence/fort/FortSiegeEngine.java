/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.world.residence.fort;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.ResidenceSiegeMusicList;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeGuardManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager.SiegeSpawn;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2FortCommanderInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan.SiegeClanType;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.player.teleport.TeleportWhoType;
import dwo.gameserver.model.world.npc.CombatFlag;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.ResidenceType;
import dwo.gameserver.model.world.residence.Siegable;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class FortSiegeEngine implements Siegable
{
	protected static final Logger _log = LogManager.getLogger(FortSiegeEngine.class);
	private final Fort _fort;
	// Fort setting
	protected FastList<L2Spawn> _commanders = new FastList<>();
	ScheduledFuture<?> _siegeEnd;
	ScheduledFuture<?> _siegeRestore;
	ScheduledFuture<?> _siegeStartTask;
	private List<L2SiegeClan> _attackerClans = new FastList<>();
	private boolean _isInProgress;
	private FortSiegeGuardManager _siegeGuardManager;

	public FortSiegeEngine(Fort fort)
	{
		_fort = fort;

		checkAutoTask();
		FortSiegeManager.getInstance().addSiege(this);
	}

	/**
	 * Начать осаду в текущем форте
	 */
	@Override
	public void startSiege()
	{
		if(!_isInProgress)
		{
			if(_siegeStartTask != null) // used admin command "admin_startfortsiege"
			{
				_siegeStartTask.cancel(true);
				_fort.despawnSiege10MinMerchants();
			}
			_siegeStartTask = null;

			if(_attackerClans.isEmpty())
			{
				return;
			}

			_isInProgress = true; // Flag so that same siege instance cannot be started again

			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(TeleportWhoType.ATTACKER, TeleportWhereType.TOWN); // Teleport to the closest town

			_fort.despawnOnSiegeDespawnNpcs(); // Despawn NPC commanders
			spawnCommanders(); // Spawn commanders
			_fort.resetDoors(); // Spawn door
			spawnSiegeGuards(); // Spawn siege guard
			_fort.setVisibleFlag(false);
			_fort.getZone().setSiegeInstance(this);
			_fort.getZone().setIsSiegeActive(true);
			_fort.getZone().updateZoneStatusForCharactersInside();

			// Schedule a task to prepare auto siege end
			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(), FortSiegeManager.getInstance().getSiegeLength() * 60 * 1000L); // Prepare auto end task

			announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_S1_HAS_BEGUN).addCastleId(_fort.getFortId()));

			// "Заводим" плейлист для осады
			for(int time : ResidenceSiegeMusicList.getInstance().getSiegeMusicFor(ResidenceType.FORT).keySet())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new PlaySiegeMusic(ResidenceSiegeMusicList.getInstance().getSiegeMusicFor(ResidenceType.FORT).get(time)), time * 1000 + 1000);
			}
			saveFortSiege();

			_log.log(Level.INFO, "Siege of " + _fort.getName() + " fort started.");
		}
	}

	/**
	 * Окончание осады
	 */
	@Override
	public void endSiege()
	{
		if(_isInProgress)
		{
			_isInProgress = false; // Flag so that siege instance can be started
			announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addCastleId(_fort.getFortId()));

			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			unSpawnFlags();

			updatePlayerSiegeStateFlags(true);

			int ownerId = -1;
			if(_fort.getOwnerClan() != null)
			{
				ownerId = _fort.getOwnerClan().getClanId();
			}
			_fort.getZone().banishForeigners(ownerId);
			_fort.getZone().setIsSiegeActive(false);
			_fort.getZone().updateZoneStatusForCharactersInside();
			_fort.getZone().setSiegeInstance(null);

			saveFortSiege(); // Save fort specific data
			clearSiegeClan(); // Clear siege clan from db
			removeCommanders(); // Remove commander from this fort

			_fort.spawnOnSiegeDespawnNpcs(); // Spawn NPC commanders
			getSiegeGuardManager().unspawnOnSiegeGuard(); // Remove all spawned siege guard from this fort
			_fort.resetDoors(); // Respawn door to fort

			ThreadPoolManager.getInstance().scheduleGeneral(new Schedule10MinMerchantSpawn(), FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay() * 60 * 1000L); // Prepare 3hr task for suspicious merchant respawn
			setSiegeDateTime(true); // store suspicious merchant spawn in DB

			if(_siegeEnd != null)
			{
				_siegeEnd.cancel(true);
				_siegeEnd = null;
			}
			if(_siegeRestore != null)
			{
				_siegeRestore.cancel(true);
				_siegeRestore = null;
			}

			if(_fort.getOwnerClan() != null && _fort.getFlagPole().getMeshIndex() == 0)
			{
				_fort.setVisibleFlag(true);
			}

			// Уведомляем музычкой членов клана-победителя :)
			if(_fort.getOwnerClan() != null)
			{
				PlaySound ps = new PlaySound(1, "siege_victory", 0, 0, 0, 0, 0);

				_fort.getOwnerClan().getOnlineMembers(-1).stream().filter(member -> member != null).forEach(member -> member.sendPacket(ps));
			}

			// Деактивируем контрольную комнату
			_fort.setControlRoomDeactivated(false);
			_log.log(Level.INFO, "Siege of " + _fort.getName() + " fort finished.");
		}
	}

	@Override
	public L2SiegeClan getAttackerClan(int clanId)
	{
		for(L2SiegeClan attackerClan : _attackerClans)
		{
			if(attackerClan != null && attackerClan.getClanId() == clanId)
			{
				return attackerClan;
			}
		}
		return null;
	}

	@Override
	public L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if(clan == null)
		{
			return null;
		}

		return getAttackerClan(clan.getClanId());
	}

	@Override
	public List<L2SiegeClan> getAttackerClans()
	{
		return _attackerClans;
	}

	/**
	 * @return список игроков, которые зарегистрированы как нападающие на форт
	 */
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		for(L2SiegeClan siegeclan : _attackerClans)
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for(L2PcInstance player : clan.getOnlineMembers(0))
			{
				if(player == null)
				{
					continue;
				}

				if(player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}

	/**
	 * @param clan The L2Clan of the player
	 * @return {@code true} если проверяемый клан является нападающим на форт
	 */
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return getAttackerClan(clan) != null;
	}

	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}

	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		return null;
	}

	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		return null;
	}

	/**
	 * @param clan проверяемый клан
	 * @return {@code true} если проверяемый клан является защитником форта
	 */
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return clan != null && _fort.getOwnerClan().equals(clan);

	}

	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		if(clan != null)
		{
			L2SiegeClan attackerClan = getAttackerClan(clan);
			if(attackerClan != null)
			{
				return attackerClan.getFlag();
			}
		}

		return null;
	}

	@Override
	public Calendar getSiegeDate()
	{
		return _fort.getSiegeDate();
	}

	@Override
	public boolean giveFame()
	{
		return true;
	}

	@Override
	public int getFameFrequency()
	{
		return Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	}

	@Override
	public int getFameAmount()
	{
		return Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	}

	@Override
	public void updateSiege()
	{
	}

	/**
	 * Announce to player.<BR><BR>
	 * @param sm the system message to send to player
	 */
	public void announceToPlayer(SystemMessage sm)
	{
		// announce messages only for participants
		L2Clan clan;
		for(L2SiegeClan siegeClan : _attackerClans)
		{
			clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			clan.getOnlineMembers(0).stream().filter(member -> member != null).forEach(member -> member.sendPacket(sm));
		}
		if(_fort.getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(_fort.getOwnerClan().getClanId());
			clan.getOnlineMembers(0).stream().filter(member -> member != null).forEach(member -> member.sendPacket(sm));
		}
	}

	public void announceToPlayer(SystemMessage sm, String s)
	{
		sm.addString(s);
		announceToPlayer(sm);
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for(L2SiegeClan siegeclan : _attackerClans)
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for(L2PcInstance member : clan.getOnlineMembers(0))
			{
				if(member == null)
				{
					continue;
				}

				if(clear)
				{
					member.setSiegeSide(PlayerSiegeSide.NONE);
					member.setActiveSiegeId(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeSide(PlayerSiegeSide.ATTACKER);
					member.setActiveSiegeId(_fort.getFortId());
					if(checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
		if(_fort.getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(_fort.getOwnerClan().getClanId());
			for(L2PcInstance member : clan.getOnlineMembers(0))
			{
				if(member == null)
				{
					continue;
				}

				if(clear)
				{
					member.setSiegeSide(PlayerSiegeSide.NONE);
					member.setActiveSiegeId(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeSide(PlayerSiegeSide.DEFENDER);
					member.setActiveSiegeId(_fort.getFortId());
					if(checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
	}

	/**
	 * @param object проверяемый объект
	 * @return {@code true} если указанный объект находится в зоне осады форта
	 */
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * @param x координата проверяемого объекта
	 * @param y координата проверяемого объекта
	 * @param z координата проверяемого объекта
	 * @return {@code true} если обьект с указанными кооринатами находится в зоне осады форта
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _isInProgress && _fort.checkIfInZone(x, y, z); // Fort zone during siege
	}

	/***
	 * Удалить все зарегистрированные на осаду кланы из базы
	 */
	public void clearSiegeClan()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, _fort.getFortId());
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			if(_fort.getOwnerClan() != null)
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?");
				statement.setInt(1, _fort.getOwnerClan().getClanId());
				statement.execute();
				DatabaseUtils.closeStatement(statement);
			}

			_attackerClans.clear();

			// if siege is in progress, end siege
			if(_isInProgress)
			{
				endSiege();
			}

			// if siege isnt in progress (1hr waiting time till siege starts), cancel waiting time
			if(_siegeStartTask != null)
			{
				_siegeStartTask.cancel(true);
				_siegeStartTask = null;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	/***
	 * Сброс тайм-стампа начала следующей осады форта
	 */
	private void clearSiegeDate()
	{
		_fort.getSiegeDate().setTimeInMillis(0);
	}

	/**
	 * @return список игроков в зоне форта
	 */
	public List<L2PcInstance> getPlayersInZone()
	{
		return _fort.getZone().getPlayersInside();
	}

	/**
	 * @return list of L2PcInstance owning the fort in the zone.
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		if(_fort.getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(_fort.getOwnerClan().getClanId());
			if(!clan.equals(_fort.getOwnerClan()))
			{
				return null;
			}

			for(L2PcInstance player : clan.getOnlineMembers(0))
			{
				if(player == null)
				{
					continue;
				}

				if(player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}

	/**
	 * Обработка события убийства коммандира форта
	 * @param instance инстанс убитого коммандира
	 */
	public void killedCommander(L2FortCommanderInstance instance)
	{
		if(_commanders != null && _fort != null && !_commanders.isEmpty())
		{
			L2Spawn spawn = instance.getSpawn();
			if(spawn != null)
			{
				FastList<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fort.getFortId());
				for(SiegeSpawn spawn2 : commanders)
				{
					if(spawn2.getNpcId() == spawn.getNpcId())
					{
						NpcStringId npcString = null;
						switch(spawn2.getId())
						{
							case 1:
								npcString = NpcStringId.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;
								break;
							case 2:
								npcString = NpcStringId.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;
								break;
							case 3:
								npcString = NpcStringId.AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;
								break;
							case 4:
								npcString = NpcStringId.I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;
								break;
						}
						if(npcString != null)
						{
							instance.broadcastPacket(new NS(instance.getObjectId(), ChatType.SHOUT, instance.getNpcId(), npcString));
						}
					}
				}
				_commanders.remove(spawn);

				// Проверяем условия для спауна знамени
				checkToSpawnFlag();
			}
			else
			{
				_log.log(Level.WARN, "FortSiegeEngine.killedCommander(): killed commander, but commander not registered for fortress. NpcId: " + instance.getNpcId() + " FortId: " + _fort.getFortId());
			}
		}
	}

	/***
	 * Проверка на соблюденные условия для спауна знамени
	 */
	public void checkToSpawnFlag()
	{
		if(_fort.getFortType() == FortType.SMALL ? _commanders.isEmpty() : _commanders.isEmpty() && _fort.isControlRoomDeactivated())
		{
			// Спауним знамя форта
			spawnFlag(_fort.getFortId());

			// Останавливаем респаун дверей и коммандиров
			if(_siegeRestore != null)
			{
				_siegeRestore.cancel(true);
			}

			// Открываем двери главного здания
			for(L2DoorInstance door : _fort.getDoors())
			{
				if(!door.isCommanderDoor())
				{
					continue;
				}

				door.openMe();
			}
			_fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.ALL_BARRACKS_OCCUPIED));
		}
		// Если коммандиры еще есть, то запускаем таск на респаун коммандиров и дверей
		else if(_siegeRestore == null)
		{
			_fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
			_siegeRestore = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSiegeRestore(), FortSiegeManager.getInstance().getCountDownLength() * 60 * 1000L);
		}
		else
		{
			_fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
		}
	}

	/**
	 * Remove the flag that was killed
	 * @param flag
	 */
	public void killedFlag(L2Npc flag)
	{
		if(flag == null)
		{
			return;
		}

		for(L2SiegeClan clan : _attackerClans)
		{
			if(clan.removeFlag(flag))
			{
				return;
			}
		}
	}

	/**
	 * Register clan as attacker<BR><BR>
	 * @param player The L2PcInstance of the player trying to register
	 * @param force
	 * @return
	 */
	public boolean registerAttacker(L2PcInstance player, boolean force)
	{
		if(player.getClan() == null)
		{
			return false;
		}

		if(force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan()); // Save to database
			// if the first registering we start the timer
			if(_attackerClans.size() == 1)
			{
				if(!force)
				{
					player.reduceAdena(ProcessType.FORT, 250000, null, true);
				}
				startAutoTask(true);
			}
			return true;
		}
		return false;
	}

	/**
	 * Remove clan from siege<BR><BR>
	 * This function does not do any checks and should not be called from bypass !
	 *
	 * @param clanId The int of player's clan id
	 */
	private void removeSiegeClan(int clanId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = clanId != 0 ? con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? AND clan_id=?") : con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");

			statement.setInt(1, _fort.getFortId());
			if(clanId != 0)
			{
				statement.setInt(2, clanId);
			}
			statement.execute();

			loadSiegeClan();
			if(_attackerClans.isEmpty())
			{
				if(_isInProgress)
				{
					endSiege();
				}
				else
				{
					saveFortSiege(); // Clear siege time in DB
				}

				if(_siegeStartTask != null)
				{
					_siegeStartTask.cancel(true);
					_siegeStartTask = null;
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on removeSiegeClan: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Remove clan from siege<BR><BR>
	 * @param clan The clan being removed
	 */
	public boolean removeSiegeClan(L2Clan clan)
	{
		if(clan == null || clan.getFortId() == _fort.getFortId() || !FortSiegeManager.getInstance().checkIsRegistered(clan, _fort.getFortId()))
		{
			return false;
		}

		removeSiegeClan(clan.getClanId());
		return true;
	}

	/**
	 * Start the auto tasks
	 */
	public void checkAutoTask()
	{
		if(_siegeStartTask != null) //safety check
		{
			return;
		}

		long delay = _fort.getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

		if(delay < 0)
		{
			// siege time in past
			saveFortSiege();
			clearSiegeClan(); // remove all clans

			// spawn suspicious merchant immediately
			ThreadPoolManager.getInstance().executeTask(new Schedule10MinMerchantSpawn());
		}
		else
		{
			loadSiegeClan();
			// no attackers - waiting for suspicious merchant spawn
			if(_attackerClans.isEmpty())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Schedule10MinMerchantSpawn(), delay);
			}
			else
			{
				// preparing start siege task
				if(delay > 3600000) // more than hour, how this can happens ? spawn suspicious merchant
				{
					ThreadPoolManager.getInstance().executeTask(new Schedule10MinMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(3600), delay - 3600000);
				}
				if(delay > 600000) // more than 10 min, spawn suspicious merchant
				{
					ThreadPoolManager.getInstance().executeTask(new Schedule10MinMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(600), delay - 600000);
				}
				else if(delay > 300000) // more than 5 min
				{
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(300), delay - 300000);
				}
				else
				{
					_siegeStartTask = delay > 60000 ? ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(60), delay - 60000) : ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(60), 0);
				}

				_log.log(Level.INFO, "Siege of " + _fort.getName() + " fort: " + _fort.getSiegeDate().getTime());
			}
		}
	}

	/**
	 * Start the auto tasks
	 * @param setTime
	 */
	public void startAutoTask(boolean setTime)
	{
		if(_siegeStartTask != null)
		{
			return;
		}

		if(setTime)
		{
			setSiegeDateTime(false);
		}

		if(_fort.getOwnerClan() != null)
		{
			_fort.getOwnerClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.A_FORTRESS_IS_UNDER_ATTACK));
		}

		// Execute siege auto start
		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(3600), 0);
	}

	/**
	 * Teleport players
	 * @param teleportWho
	 * @param teleportWhere
	 */
	public void teleportPlayer(TeleportWhoType teleportWho, TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch(teleportWho)
		{
			case OWNER:
				players = getOwnersInZone();
				break;
			case ATTACKER:
				players = getAttackersInZone();
				break;
			default:
				players = getPlayersInZone();
		}

		for(L2PcInstance player : players)
		{
			if(player.isGM() || player.isInJail())
			{
				continue;
			}

			player.teleToLocation(teleportWhere);
		}
	}

	/**
	 * Add clan as attacker<BR><BR>
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(int clanId)
	{
		_attackerClans.add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}

	/**
	 * @param player The L2PcInstance of the player trying to register
	 * @return {@code true} if the player can register.
	 */
	public boolean checkIfCanRegister(L2PcInstance player)
	{
		for(Fort fort : FortManager.getInstance().getForts())
		{
			if(fort.getSiege().getAttackerClan(player.getClanId()) != null)
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
				return false;
			}
			if(fort.getOwnerClan().equals(player.getClan()) && (fort.getSiege()._isInProgress || fort.getSiege()._siegeStartTask != null))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
				return false;
			}
		}
		return true;
	}

	private void setSiegeDateTime(boolean merchant)
	{
		Calendar newDate = Calendar.getInstance();
		if(merchant)
		{
			newDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay());
		}
		else
		{
			newDate.add(Calendar.MINUTE, 60);
		}
		_fort.setSiegeDate(newDate);
		_fort.save();
	}

	/***
	 * Загрузка всех кланов, участвующих в осаде из базы данных
	 */
	private void loadSiegeClan()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			_attackerClans.clear();

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, _fort.getFortId());
			rs = statement.executeQuery();

			while(rs.next())
			{
				addAttacker(rs.getInt("clan_id"));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/***
	 * Удаление всех заспауненных коммандиров
	 */
	private void removeCommanders()
	{
		if(_commanders != null && !_commanders.isEmpty())
		{
			// Remove all instance of commanders for this fort
			_commanders.stream().filter(spawn -> spawn != null).forEach(spawn -> {
				spawn.stopRespawn();
				if(spawn.getLastSpawn() != null)
				{
					spawn.getLastSpawn().getLocationController().delete();
				}
			});
			_commanders.clear();
		}
	}

	/***
	 * Удаление всех заспауненных флагов
	 */
	private void removeFlags()
	{
		_attackerClans.stream().filter(attackerClan -> attackerClan != null).forEach(L2SiegeClan::removeFlags);
	}

	/***
	 * Save fort siege related to database
	 */
	private void saveFortSiege()
	{
		clearSiegeDate(); // clear siege date
		_fort.save();
	}

	/**
	 * Сохранение регистрации клана на осаду в базу данных
	 * @param clan регистрирующийся клан
	 */
	private void saveSiegeClan(L2Clan clan)
	{
		if(_attackerClans.size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) values (?,?)");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, _fort.getFortId());
			statement.execute();

			addAttacker(clan.getClanId());
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: saveSiegeClan(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/***
	 * Спаун коммандиров
	 */
	private void spawnCommanders()
	{
		//Set commanders array size if one does not exist
		try
		{
			_commanders.clear();
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			for(SiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(_fort.getFortId()))
			{
				template1 = NpcTable.getInstance().getTemplate(_sp.getNpcId());
				if(template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(1);
					spawnDat.setLocx(_sp.getLocation().getX());
					spawnDat.setLocy(_sp.getLocation().getY());
					spawnDat.setLocz(_sp.getLocation().getZ());
					spawnDat.setHeading(_sp.getLocation().getHeading());
					spawnDat.setRespawnDelay(60);
					spawnDat.doSpawn();
					spawnDat.stopRespawn();
					_commanders.add(spawnDat);
				}
				else
				{
					_log.log(Level.WARN, "FortSiegeEngine.spawnCommander: Data missing in NPC table for ID: " + _sp.getNpcId() + '.');
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "FortSiegeEngine.spawnCommander: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}

	private void spawnFlag(int Id)
	{
		for(CombatFlag cf : FortSiegeManager.getInstance().getFlagList(Id))
		{
			cf.spawnMe();
		}
	}

	private void unSpawnFlags()
	{
		if(FortSiegeManager.getInstance().getFlagList(_fort.getFortId()) == null)
		{
			return;
		}

		for(CombatFlag cf : FortSiegeManager.getInstance().getFlagList(_fort.getFortId()))
		{
			cf.unSpawnMe();
		}
	}

	/**
	 * Spawn siege guard
	 */
	private void spawnSiegeGuards()
	{
		getSiegeGuardManager().spawnOnSiegeGuard();
	}

	public Fort getFort()
	{
		return _fort;
	}

	public boolean isInProgress()
	{
		return _isInProgress;
	}

	public FortSiegeGuardManager getSiegeGuardManager()
	{
		if(_siegeGuardManager == null)
		{
			_siegeGuardManager = new FortSiegeGuardManager(_fort);
		}

		return _siegeGuardManager;
	}

	/***
	 * Перезагрузка всех коммандиров и сброс состояния дверей
	 */
	public void resetSiege()
	{
		removeCommanders();
		spawnCommanders();
		_fort.resetDoors();
	}

	public List<L2Spawn> getCommanders()
	{
		return _commanders;
	}

	public class PlaySiegeMusic implements Runnable
	{
		private String musicName;

		public PlaySiegeMusic(String name)
		{
			musicName = name;
		}

		@Override
		public void run()
		{
			if(isInProgress())
			{
				PlaySound ps = new PlaySound(1, musicName.trim(), 0, 0, 0, 0, 0);

				L2Clan clan;
				if(getFort().getOwnerClan() != null)
				{
					clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
					clan.getOnlineMembers(-1).stream().filter(clanMember -> clanMember != null).forEach(clanMember -> clanMember.sendPacket(ps));
				}

				for(L2SiegeClan attackerClan : getAttackerClans())
				{
					clan = ClanTable.getInstance().getClan(attackerClan.getClanId());
					clan.getOnlineMembers(-1).stream().filter(member -> member != null).forEach(member -> member.sendPacket(ps));
				}
			}
		}

	}

	public class ScheduleEndSiegeTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInProgress())
			{
				return;
			}

			try
			{
				_siegeEnd = null;
				endSiege();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: ScheduleEndSiegeTask() for Fort: " + _fort.getName() + ' ' + e.getMessage(), e);
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		private final int _time;

		public ScheduleStartSiegeTask(int time)
		{
			_fortInst = _fort;
			_time = time;
		}

		@Override
		public void run()
		{
			if(isInProgress())
			{
				return;
			}

			try
			{
				if(_time == 3600) // 1hr remains
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(600), 3000000); // Prepare task for 10 minutes left.
				}
				else if(_time == 600) // 10min remains
				{
					getFort().despawnSiege10MinMerchants();
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(10));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(300), 300000); // Prepare task for 5 minutes left.
				}
				else if(_time == 300) // 5min remains
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(5));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(60), 240000); // Prepare task for 1 minute left.
				}
				else if(_time == 60) // 1min remains
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(1));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(30), 30000); // Prepare task for 30 seconds left.
				}
				else if(_time == 30) // 30seconds remains
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(30));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(10), 20000); // Prepare task for 10 seconds left.
				}
				else if(_time == 10) // 10seconds remains
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(10));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(5), 5000); // Prepare task for 5 seconds left.
				}
				else if(_time == 5) // 5seconds remains
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(5));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(1), 4000); // Prepare task for 1 seconds left.
				}
				else if(_time == 1) // 1seconds remains
				{
					announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(1));
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(0), 1000); // Prepare task start siege.
				}
				else if(_time == 0)// start siege
				{
					_fortInst.getSiege().startSiege();
				}
				else
				{
					_log.log(Level.WARN, "Exception: ScheduleStartSiegeTask(): unknown siege time: " + _time);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: ScheduleStartSiegeTask() for Fort: " + _fortInst.getName() + ' ' + e.getMessage(), e);
			}
		}
	}

	public class Schedule10MinMerchantSpawn implements Runnable
	{
		@Override
		public void run()
		{
			if(isInProgress())
			{
				return;
			}

			try
			{
				_fort.spawnSiege10MinMerchants();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: Schedule10MinMerchantSpawn() for Fort: " + _fort.getName() + ' ' + e.getMessage(), e);
			}
		}
	}

	public class ScheduleSiegeRestore implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInProgress())
			{
				return;
			}

			try
			{
				_siegeRestore = null;
				resetSiege();
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.BARRACKS_FUNCTION_RESTORED));
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: ScheduleSiegeRestore() for Fort: " + _fort.getName() + ' ' + e.getMessage(), e);
			}
		}
	}
}