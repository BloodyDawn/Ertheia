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
package dwo.gameserver.model.world.residence.castle;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Castles;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.ResidenceSiegeMusicList;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.castle.CastleMercTicketManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeGuardManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2ControlTowerInstance;
import dwo.gameserver.model.actor.instance.L2FlameTowerInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan.SiegeClanType;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.player.teleport.TeleportWhoType;
import dwo.gameserver.model.world.npc.spawn.CastleTowerSpawn;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.ResidenceType;
import dwo.gameserver.model.world.residence.Siegable;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.CastleSiegeInfo;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.RelationChanged;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class CastleSiegeEngine implements Siegable
{
	// typeId's
	public static final byte OWNER = -1;
	public static final byte DEFENDER = 0;
	public static final byte ATTACKER = 1;
	public static final byte DEFENDER_NOT_APPROWED = 2;
	protected static final Logger _log = LogManager.getLogger(CastleSiegeEngine.class);
	// must support Concurrent Modifications
	private final List<L2SiegeClan> _attackerClans = new FastList<>();
	private final List<L2SiegeClan> _defenderClans = new FastList<>();
	private final List<L2SiegeClan> _defenderWaitingClans = new FastList<>();
	// Castle setting
	private final List<L2ControlTowerInstance> _controlTowers = new ArrayList<>();
	private final List<L2FlameTowerInstance> _flameTowers = new ArrayList<>();
	private final Castle _castle;
	protected boolean _isRegistrationOver;
	protected Calendar _siegeEndDate;
	protected ScheduledFuture<?> _scheduledStartSiegeTask;
	protected int _firstOwnerClanId = -1;
	private int _controlTowerCount;
	private boolean _isInProgress;
	private boolean _isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	private CastleSiegeGuardManager _siegeGuardManager;

	public CastleSiegeEngine(Castle castle)
	{
		_castle = castle;
		_siegeGuardManager = new CastleSiegeGuardManager(getCastle());

		startAutoTask();
	}

	private void removeDefender(L2SiegeClan sc)
	{
		if(sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}

	private void removeAttacker(L2SiegeClan sc)
	{
		if(sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}

	private void addDefender(L2SiegeClan sc, SiegeClanType type)
	{
		if(sc == null)
		{
			return;
		}
		sc.setType(type);
		getDefenderClans().add(sc);
	}

	private void addAttacker(L2SiegeClan sc)
	{
		if(sc == null)
		{
			return;
		}
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}

	/**
	 * When control of castle changed during siege<BR><BR>
	 */
	public void midVictory()
	{
		if(_isInProgress) // CastleSiegeEngine still in progress
		{
			if(getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs(); // Remove all merc entry from db
			}

			if(getDefenderClans().isEmpty() && // If defender doesn't exist (Pc vs Npc)
				getAttackerClans().size() == 1 // Only 1 attacker
				)
			{
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			if(getCastle().getOwnerId() > 0)
			{
				int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				if(getDefenderClans().isEmpty()) // If defender doesn't exist (Pc vs Npc)
				// and only an alliance attacks
				{
					// The player's clan is in an alliance
					if(allyId != 0)
					{
						boolean allinsamealliance = true;
						for(L2SiegeClan sc : getAttackerClans())
						{
							if(sc != null)
							{
								if(ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
								{
									allinsamealliance = false;
								}
							}
						}
						if(allinsamealliance)
						{
							L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							return;
						}
					}
				}

				getDefenderClans().stream().filter(sc -> sc != null).forEach(sc -> {
					removeDefender(sc);
					addAttacker(sc);
				});

				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);

				// The player's clan is in an alliance
				for(L2Clan clan : ClanTable.getInstance().getClanAllies(allyId))
				{
					L2SiegeClan sc = getAttackerClan(clan.getClanId());
					if(sc != null)
					{
						removeAttacker(sc);
						addDefender(sc, SiegeClanType.DEFENDER);
					}
				}
				teleportPlayer(TeleportWhoType.ATTACKER, TeleportWhereType.SIEGE_FLAG); // Teleport to the second closest town
				teleportPlayer(TeleportWhoType.SPECTATOR, TeleportWhereType.TOWN); // Teleport to the second closest town

				removeDefenderFlags(); // Removes defenders' flags
				getCastle().removeAllUpgrades(); // Remove all castle upgrade
				getCastle().spawnDoor(true); // Respawn door to castle but make them weaker (50% hp)
				removeTowers(); // Remove all towers from this castle
				_controlTowerCount = 0;// Each new siege midvictory CT are completely respawned.
				spawnControlTower();
				spawnFlameTower();

				updatePlayerSiegeStateFlags(false);
			}
		}
	}

	/**
	 * Запуск осады замка
	 */
	@Override
	public void startSiege()
	{
		if(!_isInProgress)
		{
			_firstOwnerClanId = getCastle().getOwnerId();

			if(getAttackerClans().isEmpty())
			{
				SystemMessage sm;
				if(_firstOwnerClanId <= 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
					L2Clan ownerClan = ClanTable.getInstance().getClan(_firstOwnerClanId);
					ownerClan.increaseBloodAllianceCount();
				}
				sm.addCastleId(getCastle().getCastleId());
				Announcements.getInstance().announceToAll(sm);
				saveCastleSiege();
				return;
			}

			_isNormalSide = true; // Atk is now atk
			_isInProgress = true; // Flag so that same siege instance cannot be started again

			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(TeleportWhoType.ATTACKER, TeleportWhereType.TOWN); // Teleport to the closest town

			_controlTowerCount = 0;
			spawnControlTower(); // Spawn control tower
			spawnFlameTower(); // Spawn control tower

			getCastle().spawnDoor(); // Spawn door
			spawnSiegeGuard(); // Spawn siege guard
			CastleMercTicketManager.getInstance().deleteTickets(getCastle().getCastleId()); // remove the tickets from the ground
			getCastle().getZone().setSiegeInstance(this);
			getCastle().getZone().setIsSiegeActive(true);
			getCastle().getZone().updateZoneStatusForCharactersInside();

			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, CastleSiegeManager.getInstance().getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000); // Prepare auto end task

			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED).addString(getCastle().getName()));

			// "Заводим" плейлист для осады
			for(int time : ResidenceSiegeMusicList.getInstance().getSiegeMusicFor(ResidenceType.CASTLE).keySet())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new PlaySiegeMusic(ResidenceSiegeMusicList.getInstance().getSiegeMusicFor(ResidenceType.CASTLE).get(time)), time * 1000 + 1000);
			}

			HookManager.getInstance().notifyEvent(HookType.ON_SIEGE_START, null, getCastle());
		}
	}

	@Override
	public void endSiege()
	{
		if(_isInProgress)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
			sm.addString(getCastle().getName());
			Announcements.getInstance().announceToAll(sm);

			if(getCastle().getOwnerId() > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
				sm.addString(clan.getName());
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);

				if(clan.getClanId() == _firstOwnerClanId)
				{
					// Владелец замка не сменился
					clan.increaseBloodAllianceCount();
				}
				else
				{
					for(L2ClanMember member : clan.getMembers())
					{
						if(member != null)
						{
							L2PcInstance player = member.getPlayerInstance();
							if(player != null && player.isNoble())
							{
								HeroManager.getInstance().setCastleTaken(player.getObjectId(), getCastle().getCastleId());
							}
						}
					}
				}
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);
			}

			getCastle().updateClansReputation();
			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			teleportPlayer(TeleportWhoType.ATTACKER, TeleportWhereType.TOWN); // Teleport to the second closest town
			teleportPlayer(TeleportWhoType.DEFENDER_NOT_OWNER, TeleportWhereType.TOWN); // Teleport to the second closest town
			teleportPlayer(TeleportWhoType.SPECTATOR, TeleportWhereType.TOWN); // Teleport to the second closest town
			_isInProgress = false; // Flag so that siege instance can be started
			updatePlayerSiegeStateFlags(true);
			saveCastleSiege(); // Save castle specific data
			clearSiegeClan(); // Clear siege clan from db
			removeTowers(); // Remove all towers from this castle

			_siegeGuardManager.unspawnSiegeGuard(); // Remove all spawned siege guard from this castle

			if(getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();

				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
				msg.addString(ClanTable.getInstance().getClan(getCastle().getOwnerId()).getName());
				msg.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(msg);

				// Уведомляем музычкой членов клана-победителя :)
				PlaySound ps = new PlaySound(1, "siege_victory", 0, 0, 0, 0, 0);

				ClanTable.getInstance().getClan(getCastle().getOwnerId()).getOnlineMembers(-1).stream().filter(member -> member != null).forEach(member -> member.sendPacket(ps));
			}
			getCastle().spawnDoor(); // Respawn door to castle
			getCastle().getZone().setIsSiegeActive(false);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			getCastle().getZone().setSiegeInstance(null);

			HookManager.getInstance().notifyEvent(HookType.ON_SIEGE_END, null, getCastle());
		}
	}

	@Override
	public L2SiegeClan getAttackerClan(int clanId)
	{
		for(L2SiegeClan sc : getAttackerClans())
		{
			if(sc != null && sc.getClanId() == clanId)
			{
				return sc;
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
		if(_isNormalSide)
		{
			return _attackerClans;
		}
		return _defenderClans;
	}

	/**
	 * @return list of L2PcInstance registered as attacker in the zone.
	 */
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		for(L2SiegeClan siegeclan : getAttackerClans())
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
	 * @return {@code true} if clan is attacker
	 * @param clan The L2Clan of the player
	 */
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return getAttackerClan(clan) != null;
	}

	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		for(L2SiegeClan sc : getDefenderClans())
		{
			if(sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		return null;
	}

	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		if(clan == null)
		{
			return null;
		}
		return getDefenderClan(clan.getClanId());
	}

	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		if(_isNormalSide)
		{
			return _defenderClans;
		}
		return _attackerClans;
	}

	/**
	 * @return {@code true} if clan is defender
	 * @param clan The L2Clan of the player
	 */
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return getDefenderClan(clan) != null;
	}

	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		if(clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if(sc != null)
			{
				return sc.getFlag();
			}
		}
		return null;
	}

	@Override
	public Calendar getSiegeDate()
	{
		return getCastle().getSiegeDate();
	}

	@Override
	public boolean giveFame()
	{
		return true;
	}

	@Override
	public int getFameFrequency()
	{
		return Config.CASTLE_ZONE_FAME_TASK_FREQUENCY;
	}

	@Override
	public int getFameAmount()
	{
		return Config.CASTLE_ZONE_FAME_AQUIRE_POINTS;
	}

	@Override
	public void updateSiege()
	{
	}

	/**
	 * Announce to player.
	 *
	 * @param message   The SystemMessage to send to player
	 * @param bothSides {@code true} if broadcast to both attackers and defenders. {@code false} if only to defenders.
	 */
	public void announceToPlayer(SystemMessage message, boolean bothSides)
	{
		for(L2SiegeClan siegeClans : getDefenderClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
			clan.getOnlineMembers(0).stream().filter(member -> member != null).forEach(member -> member.sendPacket(message));
		}

		if(bothSides)
		{
			for(L2SiegeClan siegeClans : getAttackerClans())
			{
				L2Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
				clan.getOnlineMembers(0).stream().filter(member -> member != null).forEach(member -> member.sendPacket(message));
			}
		}
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for(L2SiegeClan siegeclan : getAttackerClans())
		{
			if(siegeclan == null)
			{
				continue;
			}

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
					member.setActiveSiegeId(getCastle().getCastleId());
					if(checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendUserInfo();
				for(L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					if(player == null)
					{
						continue;
					}

					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
					if(!member.getPets().isEmpty())
					{
						for(L2Summon pet : member.getPets())
						{
							player.sendPacket(new RelationChanged(pet, member.getRelation(player), member.isAutoAttackable(player)));
						}
					}
				}
			}
		}
		for(L2SiegeClan siegeclan : getDefenderClans())
		{
			if(siegeclan == null)
			{
				continue;
			}

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
					member.setSiegeSide(PlayerSiegeSide.DEFENDER);
					member.setActiveSiegeId(getCastle().getCastleId());
					if(checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendUserInfo();
				for(L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					if(player == null)
					{
						continue;
					}
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
					if(!member.getPets().isEmpty())
					{
						for(L2Summon pet : member.getPets())
						{
							player.sendPacket(new RelationChanged(pet, member.getRelation(player), member.isAutoAttackable(player)));
						}
					}
				}
			}
		}
	}

	/**
	 * Approve clan as defender for siege
	 *
	 * @param clanId The int of player's clan id
	 */
	public void approveSiegeDefenderClan(int clanId)
	{
		if(clanId <= 0)
		{
			return;
		}
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), DEFENDER, true);
		loadSiegeClan();
	}

	/**
	 * @return {@code true} if object is inside the zone
	 */
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * @return {@code true} if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _isInProgress && getCastle().checkIfInZone(x, y, z); // Castle zone during siege
	}

	/**
	 * @return {@code true} if clan is defender waiting approval
	 * @param clan The L2Clan of the player
	 */
	public boolean checkIsDefenderWaiting(L2Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}

	/**
	 * Clear all registered siege clans from database for castle
	 */
	public void clearSiegeClan()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.DELETE_SIEGE_CLANS);
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.clearParameters();

			if(getCastle().getOwnerId() > 0)
			{
				statement = con.prepareStatement(Castles.DELETE_SIEGE_CLAN);
				statement.setInt(1, getCastle().getOwnerId());
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			getAttackerClans().clear();
			getDefenderClans().clear();
			_defenderWaitingClans.clear();
		}
	}

	/**
	 * Clear all siege clans waiting for approval from database for castle
	 */
	public void clearSiegeWaitingClan()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.DELETE_SIEGE_CLANS_WAITING);
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: clearSiegeWaitingClan(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_defenderWaitingClans.clear();
		}
	}

	/**
	 * @return list of L2PcInstance registered as defender but not owner in the zone.
	 */
	public List<L2PcInstance> getDefendersButNotOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		for(L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if(clan.getClanId() == getCastle().getOwnerId())
			{
				continue;
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
	 * @return list of L2PcInstance in the zone.
	 */
	public List<L2PcInstance> getPlayersInZone()
	{
		return getCastle().getZone().getPlayersInside();
	}

	/**
	 * @return list of L2PcInstance owning the castle in the zone.
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		for(L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if(clan.getClanId() != getCastle().getOwnerId())
			{
				continue;
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
	 * @return list of L2PcInstance not registered as attacker or defender in the zone.
	 */
	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new FastList<>();

		for(L2PcInstance player : getCastle().getZone().getPlayersInside())
		{
			if(player == null)
			{
				continue;
			}

			if(!player.isInSiege())
			{
				players.add(player);
			}
		}
		return players;
	}

	/**
	 * Control Tower was killed
	 */
	public void onKillControlTower()
	{
		_controlTowerCount--;
		if(_controlTowerCount < 0)
		{
			_controlTowerCount = 0;
		}
	}

	/**
	 * Display list of registered clans
	 */
	public void listRegisteredClans(L2PcInstance player)
	{
		player.sendPacket(new CastleSiegeInfo(getCastle()));
	}

	/**
	 * Register clan as attacker
	 *
	 * @param player The L2PcInstance of the player trying to register
	 */
	public void registerAttacker(L2PcInstance player)
	{
		registerAttacker(player, false);
	}

	public void registerAttacker(L2PcInstance player, boolean force)
	{
		if(player.getClan() == null)
		{
			return;
		}
		int allyId = 0;
		if(getCastle().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		}
		if(allyId != 0)
		{
			if(player.getClan().getAllyId() == allyId && !force)
			{
				player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
				return;
			}
		}
		if(force || checkIfCanRegister(player, ATTACKER))
		{
			saveSiegeClan(player.getClan(), ATTACKER, false); // Save to database
		}
	}

	/**
	 * Register clan as defender
	 *
	 * @param player The L2PcInstance of the player trying to register
	 */
	public void registerDefender(L2PcInstance player)
	{
		registerDefender(player, false);
	}

	public void registerDefender(L2PcInstance player, boolean force)
	{
		if(getCastle().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
		}
		else if(force || checkIfCanRegister(player, DEFENDER_NOT_APPROWED))
		{
			saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROWED, false); // Save to database
		}
	}

	/**
	 * Remove clan from siege
	 *
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(int clanId)
	{
		if(clanId <= 0)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.DELETE_SIEGE_CLAN_FROM_CASTLE);
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: removeSiegeClan(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			loadSiegeClan();
		}
	}

	/**
	 * Remove clan from siege<BR><BR>
	 *
	 * @param clan The L2PcInstance of player/clan being removed
	 */
	public void removeSiegeClan(L2Clan clan)
	{
		if(clan == null || clan.getCastleId() == getCastle().getCastleId() || !CastleSiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId()))
		{
			return;
		}
		removeSiegeClan(clan.getClanId());
	}

	/**
	 * Remove clan from siege<BR><BR>
	 *
	 * @param player The L2PcInstance of player/clan being removed
	 */
	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}

	/**
	 * Start the auto tasks<BR><BR>
	 */
	public void startAutoTask()
	{
		correctSiegeDateTime();

		_log.log(Level.INFO, "CastleSiegeEngine of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());

		loadSiegeClan();

		// Schedule siege auto start
		if(_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(false);
		}
		_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000);
	}

	/**
	 * Teleport players
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
			case DEFENDER_NOT_OWNER:
				players = getDefendersButNotOwnersInZone();
				break;
			case SPECTATOR:
				players = getSpectatorsInZone();
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
	 * Add clan as attacker
	 *
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}

	/**
	 * Add clan as defender
	 *
	 * @param clanId The int of clan's id
	 */
	private void addDefender(int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add registered defender to defender list
	}

	/**
	 * Add clan as defender with the specified type
	 *
	 * @param clanId The int of clan's id
	 * @param type   the type of the clan
	 */
	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}

	/**
	 * Add clan as defender waiting approval
	 *
	 * @param clanId The int of clan's id
	 */
	private void addDefenderWaiting(int clanId)
	{
		_defenderWaitingClans.add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add registered defender to defender list
	}

	/**
	 * Return true if the player can register.<BR><BR>
	 *
	 * @param player The L2PcInstance of the player trying to register
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 */
	private boolean checkIfCanRegister(L2PcInstance player, byte typeId)
	{
		if(_isRegistrationOver)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(getCastle().getName()));
		}
		else if(_isInProgress)
		{
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		}
		else if(player.getClan() == null || player.getClan().getLevel() < CastleSiegeManager.getInstance().getSiegeClanMinLevel())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE);
		}
		else if(player.getClan().getClanId() == getCastle().getOwnerId())
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if(player.getClan().getCastleId() > 0)
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		}
		else if(CastleSiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getCastleId()))
		{
			player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		}
		// В GOD осады начинаются одновременно, можно регистрироваться хоть на все сразу
		/*else if (checkIfAlreadyRegisteredForSameHour(player.getClan()))
		{
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		}*/
		else if(typeId == ATTACKER && getAttackerClans().size() >= CastleSiegeManager.getInstance().getAttackerMaxClans())
		{
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
		}
		else if((typeId == DEFENDER || typeId == DEFENDER_NOT_APPROWED || typeId == OWNER) && getDefenderClans().size() + _defenderWaitingClans.size() >= CastleSiegeManager.getInstance().getDefenderMaxClans())
		{
			player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
		}
		else
		{
			return true;
		}

		return false;
	}

	/**
	 * @param clan The L2Clan of the player trying to register
	 * @return {@code true} if the clan has already registered to a siege for the same day.
	 */
	@Deprecated
	public boolean checkIfAlreadyRegisteredForSameHour(L2Clan clan)
	{
		for(CastleSiegeEngine siege : CastleSiegeManager.getInstance().getSieges())
		{
			if(siege.equals(this))
			{
				continue;
			}
			if(siege.getSiegeDate().get(Calendar.HOUR_OF_DAY) == getSiegeDate().get(Calendar.HOUR_OF_DAY))
			{
				if(siege.checkIsAttacker(clan))
				{
					return true;
				}
				if(siege.checkIsDefender(clan))
				{
					return true;
				}
				if(siege.checkIsDefenderWaiting(clan))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the correct siege date as Calendar
	 */
	public void correctSiegeDateTime()
	{
		boolean corrected = false;

		if(getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Since siege has past reschedule it to the next one
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}

		if(corrected)
		{
			saveSiegeDate();
		}
	}

	/**
	 * Load siege clans.
	 */
	private void loadSiegeClan()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			_defenderWaitingClans.clear();

			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if(getCastle().getOwnerId() > 0)
			{
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}

			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.LOAD_SIEGE_CLANS);
			statement.setInt(1, getCastle().getCastleId());
			rs = statement.executeQuery();

			int typeId;
			while(rs.next())
			{
				typeId = rs.getInt("type");
				if(typeId == DEFENDER)
				{
					addDefender(rs.getInt("clan_id"));
				}
				else if(typeId == ATTACKER)
				{
					addAttacker(rs.getInt("clan_id"));
				}
				else if(typeId == DEFENDER_NOT_APPROWED)
				{
					addDefenderWaiting(rs.getInt("clan_id"));
				}
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

	/**
	 * Remove all control tower spawned.
	 */
	private void removeTowers()
	{
		for(L2FlameTowerInstance ct : _flameTowers)
		{
			ct.onDelete();
		}

		for(L2ControlTowerInstance ct : _controlTowers)
		{
			ct.onDelete();
		}

		_flameTowers.clear();
		_controlTowers.clear();
	}

	/**
	 * Remove all flags.
	 */
	private void removeFlags()
	{
		getAttackerClans().stream().filter(sc -> sc != null).forEach(L2SiegeClan::removeFlags);
		getDefenderClans().stream().filter(sc -> sc != null).forEach(L2SiegeClan::removeFlags);
	}

	/**
	 * Remove flags from defenders.
	 */
	private void removeDefenderFlags()
	{
		getDefenderClans().stream().filter(sc -> sc != null).forEach(L2SiegeClan::removeFlags);
	}

	/**
	 * Save castle siege related to database.
	 */
	private void saveCastleSiege()
	{
		setNextSiegeDate(); // Set the next set date for 2 weeks from now
		// Schedule Time registration end
		getTimeRegistrationOverDate().setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		getTimeRegistrationOverDate().add(Calendar.DAY_OF_MONTH, 1);
		getCastle().setIsTimeRegistrationOver(false);

		saveSiegeDate(); // Save the new date
		startAutoTask(); // Prepare auto start siege and end registration
	}

	/**
	 * Save siege date to database.
	 */
	public void saveSiegeDate()
	{
		if(_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(true);
			_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000);
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.UPDATE_SIEGE_DATES);
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setLong(2, getTimeRegistrationOverDate().getTimeInMillis());
			statement.setString(3, String.valueOf(getIsTimeRegistrationOver()));
			statement.setInt(4, getCastle().getCastleId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Save registration to database.
	 *
	 * @param clan   The L2Clan of player
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 */
	private void saveSiegeClan(L2Clan clan, byte typeId, boolean isUpdateRegistration)
	{
		if(clan.getCastleId() > 0)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			if(typeId == DEFENDER || typeId == DEFENDER_NOT_APPROWED || typeId == OWNER)
			{
				if(getDefenderClans().size() + _defenderWaitingClans.size() >= CastleSiegeManager.getInstance().getDefenderMaxClans())
				{
					return;
				}
			}
			else
			{
				if(getAttackerClans().size() >= CastleSiegeManager.getInstance().getAttackerMaxClans())
				{
					return;
				}
			}

			con = L2DatabaseFactory.getInstance().getConnection();
			if(isUpdateRegistration)
			{
				statement = con.prepareStatement(Castles.UPDATE_SIEGE_CLAN);
				statement.setInt(1, typeId);
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement(Castles.INSERT_SIEGE_CLAN);
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, typeId);
				statement.execute();
			}

			switch(typeId)
			{
				case DEFENDER:
				case OWNER:
					addDefender(clan.getClanId());
					break;
				case ATTACKER:
					addAttacker(clan.getClanId());
					break;
				case DEFENDER_NOT_APPROWED:
					addDefenderWaiting(clan.getClanId());
					break;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Set the date for the next siege.
	 */
	private void setNextSiegeDate()
	{
		while(getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, Config.SIEGE_HOUR);
			getCastle().getSiegeDate().set(Calendar.MINUTE, 0);
			getCastle().getSiegeDate().set(Calendar.SECOND, 0);
			// set the next siege day to the next weekend
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME);
		sm.addString(getCastle().getName());
		Announcements.getInstance().announceToAll(sm);

		_isRegistrationOver = false; // Allow registration for next siege
	}

	/**
	 * Spawn control tower.
	 */
	private void spawnControlTower()
	{
		for(CastleTowerSpawn ts : CastleSiegeManager.getInstance().getControlTowers(getCastle().getCastleId()))
		{
			try
			{
				L2Spawn spawn = new L2Spawn(NpcTable.getInstance().getTemplate(ts.getNpcId()));
				spawn.setLocation(ts.getLocation());
				_controlTowers.add((L2ControlTowerInstance) spawn.doSpawn());
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getName() + ": Cannot spawn control tower! " + e);
			}
		}
		_controlTowerCount = _controlTowers.size();
	}

	/**
	 * Spawn flame tower.
	 */
	private void spawnFlameTower()
	{
		for(CastleTowerSpawn ts : CastleSiegeManager.getInstance().getFlameTowers(getCastle().getCastleId()))
		{
			try
			{
				L2Spawn spawn = new L2Spawn(NpcTable.getInstance().getTemplate(ts.getNpcId()));
				spawn.setLocation(ts.getLocation());
				L2FlameTowerInstance tower = (L2FlameTowerInstance) spawn.doSpawn();
				tower.setUpgradeLevel(ts.getUpgradeLevel());
				tower.setZoneList(ts.getZoneList());
				_flameTowers.add(tower);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getName() + ": Cannot spawn flame tower! " + e);
			}
		}
	}

	/**
	 * Spawn siege guard.<BR><BR>
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();

		// Register guard to the closest Control Tower
		// When CT dies, so do all the guards that it controls
		if(!getSiegeGuardManager().getSiegeGuardSpawn().isEmpty())
		{
			L2ControlTowerInstance closestCt;
			int x;
			int y;
			int z;
			double distance;
			double distanceClosest = 0;
			for(L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if(spawn == null)
				{
					continue;
				}

				closestCt = null;
				distanceClosest = Integer.MAX_VALUE;

				x = spawn.getLocx();
				y = spawn.getLocy();
				z = spawn.getLocz();

				for(L2ControlTowerInstance ct : _controlTowers)
				{
					if(ct == null)
					{
						continue;
					}

					distance = ct.getDistanceSq(x, y, z);

					if(distance < distanceClosest)
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				if(closestCt != null)
				{
					closestCt.registerGuard(spawn);
				}
			}
		}
	}

	public int getAttackerRespawnDelay()
	{
		return CastleSiegeManager.getInstance().getAttackerRespawnDelay();
	}

	public Castle getCastle()
	{
		if(_castle == null)
		{
			return null;
		}
		return _castle;
	}

	public L2SiegeClan getDefenderWaitingClan(L2Clan clan)
	{
		if(clan == null)
		{
			return null;
		}
		return getDefenderWaitingClan(clan.getClanId());
	}

	public L2SiegeClan getDefenderWaitingClan(int clanId)
	{
		for(L2SiegeClan sc : _defenderWaitingClans)
		{
			if(sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		return null;
	}

	public List<L2SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
	}

	public boolean isInProgress()
	{
		return _isInProgress;
	}

	public boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}

	public boolean getIsTimeRegistrationOver()
	{
		return getCastle().getIsTimeRegistrationOver();
	}

	public Calendar getTimeRegistrationOverDate()
	{
		return getCastle().getTimeRegistrationOverDate();
	}

	public void endTimeRegistration(boolean automatic)
	{
		getCastle().setIsTimeRegistrationOver(true);
		if(!automatic)
		{
			saveSiegeDate();
		}
	}

	public CastleSiegeGuardManager getSiegeGuardManager()
	{
		if(_siegeGuardManager == null)
		{
			_siegeGuardManager = new CastleSiegeGuardManager(getCastle());
		}
		return _siegeGuardManager;
	}

	public int getControlTowerCount()
	{
		return _controlTowerCount;
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
				for(L2SiegeClan defenders : getDefenderClans())
				{
					clan = ClanTable.getInstance().getClan(defenders.getClanId());
					clan.getOnlineMembers(-1).stream().filter(member -> member != null).forEach(member -> member.sendPacket(ps));
				}

				for(L2SiegeClan attackers : getAttackerClans())
				{
					clan = ClanTable.getInstance().getClan(attackers.getClanId());
					clan.getOnlineMembers(-1).stream().filter(member -> member != null).forEach(member -> member.sendPacket(ps));
				}
			}
		}
	}

	public class ScheduleEndSiegeTask implements Runnable
	{
		private Castle _castleInst;

		public ScheduleEndSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}

		@Override
		public void run()
		{
			if(!isInProgress())
			{
				return;
			}

			try
			{
				long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if(timeRemaining > 3600000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(2);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				}
				else if(timeRemaining <= 3600000 && timeRemaining > 600000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(Math.round(timeRemaining / 60000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if(timeRemaining <= 600000 && timeRemaining > 300000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(Math.round(timeRemaining / 60000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				}
				else if(timeRemaining <= 300000 && timeRemaining > 10000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(Math.round(timeRemaining / 60000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				}
				else if(timeRemaining <= 10000 && timeRemaining > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT);
					sm.addNumber(Math.round(timeRemaining / 1000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining); // Prepare task for second count down
				}
				else
				{
					_castleInst.getSiege().endSiege();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while running ScheduleEndSiegeTask()", e);
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private Castle _castleInst;

		public ScheduleStartSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}

		@Override
		public void run()
		{
			_scheduledStartSiegeTask.cancel(false);
			if(isInProgress())
			{
				return;
			}

			try
			{
				if(!getIsTimeRegistrationOver())
				{
					long regTimeRemaining = getTimeRegistrationOverDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
					if(regTimeRemaining > 0)
					{
						_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), regTimeRemaining);
						return;
					}
					else
					{
						endTimeRegistration(true);
					}
				}

				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if(timeRemaining > 86400000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 86400000); // Prepare task for 24 before siege start to end registration
				}
				else if(timeRemaining <= 86400000 && timeRemaining > 13600000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
					sm.addString(getCastle().getName());
					Announcements.getInstance().announceToAll(sm);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 13600000); // Prepare task for 1 hr left before siege start.
				}
				else if(timeRemaining <= 13600000 && timeRemaining > 600000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if(timeRemaining <= 600000 && timeRemaining > 300000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				}
				else if(timeRemaining <= 300000 && timeRemaining > 10000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				}
				else if(timeRemaining <= 10000 && timeRemaining > 0)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining); // Prepare task for second count down
				}
				else
				{
					_castleInst.getSiege().startSiege();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while running ScheduleEndSiegeTask()", e);
			}
		}
	}
}