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
package dwo.gameserver.model.world.residence.clanhall;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan.SiegeClanType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.Siegable;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author BiggBoss
 */
public abstract class ClanHallSiegeEngine extends Quest implements Siegable
{
	public static final int FORTRESS_RESSISTANCE = 21;
	public static final int DEVASTATED_CASTLE = 34;
	public static final int BANDIT_STRONGHOLD = 35;
	public static final int RAINBOW_SPRINGS = 62;
	public static final int BEAST_FARM = 63;
	public static final int FORTRESS_OF_DEAD = 64;
	private static final String SQL_LOAD_ATTACKERS = "SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?";
	private static final String SQL_SAVE_ATTACKERS = "INSERT INTO clanhall_siege_attackers VALUES (?,?)";
	private static final String SQL_LOAD_GUARDS = "SELECT * FROM clanhall_siege_guards WHERE clanHallId = ?";
	protected final Logger _log;
	public ClanHallSiegable _hall;
	public ScheduledFuture<?> _siegeTask;
	public boolean _missionAccomplished;
	private FastMap<Integer, L2SiegeClan> _attackers = new FastMap<>();
	private FastList<L2Spawn> _guards;

	protected ClanHallSiegeEngine(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr);
		_log = LogManager.getLogger(ClanHallSiegeEngine.class);

		_hall = ClanHallSiegeManager.getInstance().getSiegableHall(hallId);
		_hall.setSiege(this);

		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.log(Level.INFO, _hall.getName() + " siege scheduled for: " + getSiegeDate().getTime());
		loadAttackers();
	}

	public void loadAttackers()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_LOAD_ATTACKERS);
			statement.setInt(1, _hall.getId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("attacker_id");
				L2SiegeClan clan = new L2SiegeClan(id, SiegeClanType.ATTACKER);
				_attackers.put(id, clan);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getName() + ": Could not load siege attackers!:");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void saveAttackers()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("DELETE FROM clanhall_siege_attackers WHERE clanhall_id = ?");
			statement.setInt(1, _hall.getId());
			statement.execute();

			if(!_attackers.isEmpty())
			{
				for(L2SiegeClan clan : _attackers.values())
				{
					FiltredPreparedStatement insert = con.prepareStatement(SQL_SAVE_ATTACKERS);
					insert.setInt(1, _hall.getId());
					insert.setInt(2, clan.getClanId());
					insert.execute();
					insert.close();
				}
			}
			_log.log(Level.INFO, getName() + ": Sucessfully saved attackers down to database!");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Couldnt save attacker list!", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void loadGuards()
	{
		if(_guards == null)
		{
			_guards = new FastList<>();

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(SQL_LOAD_GUARDS);
				statement.setInt(1, _hall.getId());
				rset = statement.executeQuery();
				while(rset.next())
				{
					int npcId = rset.getInt("npcId");
					L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
					L2Spawn spawn = new L2Spawn(template);
					spawn.setLocx(rset.getInt("x"));
					spawn.setLocy(rset.getInt("y"));
					spawn.setLocz(rset.getInt("z"));
					spawn.setHeading(rset.getInt("heading"));
					spawn.setRespawnDelay(rset.getInt("respawnDelay"));
					spawn.setAmount(1);
					_guards.add(spawn);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Couldnt load siege guards!:", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			}
		}
	}

	private void spawnSiegeGuards()
	{
		_guards.stream().filter(guard -> guard != null).forEach(L2Spawn::init);
	}

	private void unSpawnSiegeGuards()
	{
		if(_guards != null && !_guards.isEmpty())
		{
			_guards.stream().filter(guard -> guard != null).forEach(guard -> {
				guard.stopRespawn();
				guard.getLastSpawn().getLocationController().delete();
			});
		}
	}

	public FastMap<Integer, L2SiegeClan> getAttackers()
	{
		return _attackers;
	}

	public void prepareOwner()
	{
		if(_hall.getOwnerId() > 0)
		{
			L2SiegeClan clan = new L2SiegeClan(_hall.getOwnerId(), SiegeClanType.ATTACKER);
			_attackers.put(clan.getClanId(), new L2SiegeClan(clan.getClanId(), SiegeClanType.ATTACKER));
		}

		_hall.free();
		_hall.banishForeigners();

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Announcements.getInstance().announceToAll(msg);
		_hall.updateSiegeStatus(ClanHallSiegeStatus.WAITING_BATTLE);

		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}

	@Override
	public void startSiege()
	{
		if(_attackers.size() < 1 && _hall.getId() != 21) // Fortress of resistance dont have attacker list
		{
			onSiegeEnds();
			_attackers.clear();
			_hall.updateNextSiege();
			_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getSiegeDate().getTimeInMillis());
			_hall.updateSiegeStatus(ClanHallSiegeStatus.WAITING_BATTLE);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Announcements.getInstance().announceToAll(sm);
			return;
		}

		_hall.spawnDoor();
		loadGuards();
		spawnSiegeGuards();
		_hall.updateSiegeZone(true);

		for(L2SiegeClan sClan : getAttackerClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(sClan.getClanId());
			if(clan == null)
			{
				continue;
			}

			clan.getOnlineMembers(0).stream().filter(pc -> pc != null).forEach(pc -> {
				pc.setSiegeSide(PlayerSiegeSide.ATTACKER);
				pc.broadcastUserInfo();
				pc.setIsInHideoutSiege(true);
			});
		}

		_hall.updateSiegeStatus(ClanHallSiegeStatus.RUNNING);
		onSiegeStarts();
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEnds(), _hall.getSiegeLenght());
	}

	@Override
	public void endSiege()
	{
		SystemMessage end = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
		end.addString(_hall.getName());
		Announcements.getInstance().announceToAll(end);

		L2Clan winner = getWinner();
		SystemMessage finalMsg = null;
		if(_missionAccomplished && winner != null)
		{
			_hall.setOwner(winner);
			winner.setClanhallId(_hall.getId());
			finalMsg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
			finalMsg.addString(winner.getName());
			finalMsg.addString(_hall.getName());
			Announcements.getInstance().announceToAll(finalMsg);
		}
		else
		{
			finalMsg = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
			finalMsg.addString(_hall.getName());
			Announcements.getInstance().announceToAll(finalMsg);
		}
		_missionAccomplished = false;

		_hall.updateSiegeZone(false);
		_hall.updateNextSiege();
		_hall.spawnDoor(false);
		_hall.banishForeigners();

		for(L2SiegeClan sClan : getAttackerClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(sClan.getClanId());
			if(clan == null)
			{
				continue;
			}

			for(L2PcInstance player : clan.getOnlineMembers(0))
			{
				player.setSiegeSide(PlayerSiegeSide.NONE);
				player.broadcastUserInfo();
				player.setIsInHideoutSiege(false);
			}
		}

		// Update pvp flag for winners when siege zone becomes inactive
		_hall.getSiegeZone().getCharactersInside().stream().filter(chr -> chr != null && chr.isPlayer()).forEach(chr -> chr.getActingPlayer().getPvPFlagController().startFlag());

		_attackers.clear();

		onSiegeEnds();

		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.log(Level.INFO, "CastleSiegeEngine of " + _hall.getName() + " scheduled for: " + _hall.getSiegeDate().getTime());

		_hall.updateSiegeStatus(ClanHallSiegeStatus.REGISTERING);
		unSpawnSiegeGuards();
	}

	@Override
	public L2SiegeClan getAttackerClan(int clanId)
	{
		return _attackers.get(clanId);
	}

	@Override
	public L2SiegeClan getAttackerClan(L2Clan clan)
	{
		return getAttackerClan(clan.getClanId());
	}

	@Override
	public List<L2SiegeClan> getAttackerClans()
	{
		FastList<L2SiegeClan> result = new FastList<>();
		result.addAll(_attackers.values());
		return result;
	}

	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> list = _hall.getSiegeZone().getPlayersInside();
		List<L2PcInstance> attackers = new FastList<>();

		for(L2PcInstance pc : list)
		{
			L2Clan clan = pc.getClan();
			if(clan != null && _attackers.containsKey(clan.getClanId()))
			{
				attackers.add(pc);
			}
		}

		return attackers;
	}

	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		if(clan == null)
		{
			return false;
		}

		return _attackers.containsKey(clan.getClanId());
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

	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return false;
	}

	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		List<L2Npc> result = null;
		L2SiegeClan sClan = getAttackerClan(clan);
		if(sClan != null)
		{
			result = sClan.getFlag();
		}
		return result;
	}

	@Override
	public Calendar getSiegeDate()
	{
		return _hall.getSiegeDate();
	}

	@Override
	public boolean giveFame()
	{
		return Config.CHS_ENABLE_FAME;
	}

	@Override
	public int getFameFrequency()
	{
		return Config.CHS_FAME_FREQUENCY;
	}

	// XXX Fame settings ---------------------------

	@Override
	public int getFameAmount()
	{
		return Config.CHS_FAME_AMOUNT;
	}

	@Override
	public void updateSiege()
	{
		cancelSiegeTask();
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - 3600000);
		_log.log(Level.INFO, _hall.getName() + " siege scheduled for: " + _hall.getSiegeDate().getTime());
	}

	public void cancelSiegeTask()
	{
		if(_siegeTask != null)
		{
			_siegeTask.cancel(false);
		}
	}

	public void broadcastNpcSay(L2Npc npc, ChatType type, int messageId)
	{
		NS npcSay = new NS(npc.getObjectId(), type, npc.getNpcId(), NpcStringId.getNpcStringId(messageId));
		int sourceRegion = MapRegionManager.getInstance().getMapRegionLocId(npc);
		L2PcInstance[] charsInside = WorldManager.getInstance().getAllPlayersArray();

		for(L2PcInstance pc : charsInside)
		{
			if(pc != null && MapRegionManager.getInstance().getMapRegionLocId(pc) == sourceRegion)
			{
				pc.sendPacket(npcSay);
			}
		}
	}

	// XXX CastleSiegeEngine task and abstract methods -------------------

	public Location getInnerSpawnLoc(L2PcInstance player)
	{
		return null;
	}

	public boolean canPlantFlag()
	{
		return true;
	}

	public void onSiegeStarts()
	{
	}

	public void onSiegeEnds()
	{
	}

	public boolean doorIsAutoAttackable()
	{
		return true;
	}

	public abstract L2Clan getWinner();

	public class PrepareOwner implements Runnable
	{
		@Override
		public void run()
		{
			prepareOwner();
		}
	}

	public class SiegeStarts implements Runnable
	{
		@Override
		public void run()
		{
			startSiege();
		}
	}

	public class SiegeEnds implements Runnable
	{
		@Override
		public void run()
		{
			endSiege();
		}
	}
}