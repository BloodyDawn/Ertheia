package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class RaidBossSpawnManager
{
	protected static Map<Integer, L2RaidBossInstance> _bosses;
	protected static Map<Integer, L2Spawn> _spawns;
	protected static Map<Integer, StatsSet> _storedInfo;
	protected static Map<Integer, ScheduledFuture<?>> _schedules;
	private static Logger _log = LogManager.getLogger(RaidBossSpawnManager.class);

	private RaidBossSpawnManager()
	{
		init();
	}

	public static RaidBossSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void init()
	{
		_bosses = new FastMap<>();
		_schedules = new FastMap<>();
		_storedInfo = new FastMap<>();
		_spawns = new FastMap<>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * FROM raidboss_spawnlist ORDER BY boss_id");
			rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template;
			long respawnTime;
			while(rset.next())
			{
				template = getValidTemplate(rset.getInt("boss_id"));
				if(template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setLocx(rset.getInt("loc_x"));
					spawnDat.setLocy(rset.getInt("loc_y"));
					spawnDat.setLocz(rset.getInt("loc_z"));
					spawnDat.setAmount(rset.getInt("amount"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnMinDelay(rset.getInt("respawn_min_delay"));
					spawnDat.setRespawnMaxDelay(rset.getInt("respawn_max_delay"));
					respawnTime = rset.getLong("respawn_time");

					addNewSpawn(spawnDat, respawnTime, rset.getDouble("currentHP"), rset.getDouble("currentMP"));
				}
				else
				{
					_log.log(Level.WARN, "RaidBossSpawnManager: Could not load raidboss #" + rset.getInt("boss_id") + " from DB");
				}
			}

			_log.log(Level.INFO, "RaidBossSpawnManager: Loaded " + _bosses.size() + " Instances");
			_log.log(Level.INFO, "RaidBossSpawnManager: Scheduled " + _schedules.size() + " Instances");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "RaidBossSpawnManager: Couldnt load raidboss_spawnlist table");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while initializing RaidBossSpawnManager: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void updateStatus(L2RaidBossInstance boss, boolean isBossDead)
	{
		if(!_storedInfo.containsKey(boss.getNpcId()))
		{
			return;
		}

		StatsSet info = _storedInfo.get(boss.getNpcId());

		if(isBossDead)
		{
			boss.setRaidStatus(StatusEnum.DEAD);

			int respawnMinDelay = boss.getSpawn().getRespawnMinDelay();
			int respawnMaxDelay = boss.getSpawn().getRespawnMaxDelay();
			long respawnDelay = Rnd.get((int) (respawnMinDelay * 1000 * Config.RAID_MIN_RESPAWN_MULTIPLIER), (int) (respawnMaxDelay * 1000 * Config.RAID_MAX_RESPAWN_MULTIPLIER));
			long respawnTime = Calendar.getInstance().getTimeInMillis() + respawnDelay;

			info.set("currentHP", boss.getMaxHp());
			info.set("currentMP", boss.getMaxMp());
			info.set("respawnTime", respawnTime);

			if(!_schedules.containsKey(boss.getNpcId()) && respawnMinDelay > 0 && respawnMaxDelay > 0)
			{
				Calendar time = Calendar.getInstance();
				time.setTimeInMillis(respawnTime);
				_log.log(Level.INFO, "RaidBossSpawnManager: Updated " + boss.getName() + " respawn time to " + time.getTime());

				ScheduledFuture<?> futureSpawn;
				futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(boss.getNpcId()), respawnDelay);

				_schedules.put(boss.getNpcId(), futureSpawn);
				updateDb();
			}
		}
		else
		{
			boss.setRaidStatus(StatusEnum.ALIVE);

			info.set("currentHP", boss.getCurrentHp());
			info.set("currentMP", boss.getCurrentMp());
			info.set("respawnTime", 0L);
		}

		_storedInfo.put(boss.getNpcId(), info);
	}

	public void addNewSpawn(L2Spawn spawnDat, long respawnTime, double currentHP, double currentMP)
	{
		if(spawnDat == null)
		{
			return;
		}
		if(_spawns.containsKey(spawnDat.getNpcId()))
		{
			return;
		}

		int bossId = spawnDat.getNpcId();
		long time = Calendar.getInstance().getTimeInMillis();

		SpawnTable.getInstance().addNewSpawn(spawnDat);

		if(respawnTime == 0L || time > respawnTime)
		{
			L2RaidBossInstance raidboss;

			raidboss = bossId == 25328 ? DayNightSpawnManager.getInstance().handleBoss(spawnDat) : (L2RaidBossInstance) spawnDat.doSpawn();

			if(raidboss != null)
			{
				raidboss.setCurrentHp(currentHP);
				raidboss.setCurrentMp(currentMP);
				raidboss.setRaidStatus(StatusEnum.ALIVE);

				_bosses.put(bossId, raidboss);

				StatsSet info = new StatsSet();
				info.set("currentHP", currentHP);
				info.set("currentMP", currentMP);
				info.set("respawnTime", 0L);

				_storedInfo.put(bossId, info);
			}
		}
		else
		{
			ScheduledFuture<?> futureSpawn;
			long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();

			futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(bossId), spawnTime);

			_schedules.put(bossId, futureSpawn);
		}

		_spawns.put(bossId, spawnDat);
	}

	public void deleteSpawn(L2Spawn spawnDat, boolean updateDb)
	{
		if(spawnDat == null)
		{
			return;
		}
		if(!_spawns.containsKey(spawnDat.getNpcId()))
		{
			return;
		}

		int bossId = spawnDat.getNpcId();

		SpawnTable.getInstance().deleteSpawn(spawnDat);
		_spawns.remove(bossId);

		if(_bosses.containsKey(bossId))
		{
			_bosses.remove(bossId);
		}

		if(_schedules.containsKey(bossId))
		{
			ScheduledFuture<?> f = _schedules.get(bossId);
			f.cancel(true);
			_schedules.remove(bossId);
		}

		if(_storedInfo.containsKey(bossId))
		{
			_storedInfo.remove(bossId);
		}

		if(updateDb)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?");
				statement.setInt(1, bossId);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "RaidBossSpawnManager: Could not remove raidboss #" + bossId + " from DB: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	private void updateDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?");

			for(Map.Entry<Integer, StatsSet> integerStatsSetEntry : _storedInfo.entrySet())
			{
				if(integerStatsSetEntry.getKey() == null)
				{
					continue;
				}

				L2RaidBossInstance boss = _bosses.get(integerStatsSetEntry.getKey());

				if(boss == null)
				{
					continue;
				}

				if(boss.getRaidStatus() == StatusEnum.ALIVE)
				{
					updateStatus(boss, false);
				}

				StatsSet info = integerStatsSetEntry.getValue();

				if(info == null)
				{
					continue;
				}

				try
				{
					statement.setLong(1, info.getLong("respawnTime"));
					statement.setDouble(2, info.getDouble("currentHP"));
					statement.setDouble(3, info.getDouble("currentMP"));
					statement.setInt(4, integerStatsSetEntry.getKey());
					statement.executeUpdate();
					statement.clearParameters();
				}
				catch(SQLException e)
				{
					_log.log(Level.ERROR, "RaidBossSpawnManager: Couldnt update raidboss_spawnlist table " + e.getMessage(), e);
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "SQL error while updating RaidBoss spawn to database: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public StatusEnum getRaidBossStatusId(int bossId)
	{
		if(_bosses.containsKey(bossId))
		{
			return _bosses.get(bossId).getRaidStatus();
		}
		else
		{
			return _schedules.containsKey(bossId) ? StatusEnum.DEAD : StatusEnum.UNDEFINED;
		}
	}

	public L2NpcTemplate getValidTemplate(int bossId)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		if(template == null)
		{
			return null;
		}
		if(!template.isType("L2RaidBoss"))
		{
			return null;
		}
		return template;
	}

	public void notifySpawnNightBoss(L2RaidBossInstance raidboss)
	{
		StatsSet info = new StatsSet();
		info.set("currentHP", raidboss.getCurrentHp());
		info.set("currentMP", raidboss.getCurrentMp());
		info.set("respawnTime", 0L);

		raidboss.setRaidStatus(StatusEnum.ALIVE);

		_storedInfo.put(raidboss.getNpcId(), info);

		_log.log(Level.INFO, "Spawning Night Raid Boss " + raidboss.getName());

		_bosses.put(raidboss.getNpcId(), raidboss);
	}

	public boolean isDefined(int bossId)
	{
		return _spawns.containsKey(bossId);
	}

	public Map<Integer, L2RaidBossInstance> getBosses()
	{
		return _bosses;
	}

	public void reloadBosses()
	{
		init();
	}

	/**
	 * Saves all raidboss status and then clears all info from memory,
	 * including all schedules.
	 */
	public void cleanUp()
	{
		updateDb();

		_bosses.clear();

		if(_schedules != null)
		{
			for(Map.Entry<Integer, ScheduledFuture<?>> integerScheduledFutureEntry : _schedules.entrySet())
			{
				ScheduledFuture<?> f = integerScheduledFutureEntry.getValue();
				f.cancel(true);
			}
			_schedules.clear();
		}
		_storedInfo.clear();
		_spawns.clear();
	}

	public static enum StatusEnum
	{
		ALIVE,
		DEAD,
		UNDEFINED
	}

	private static class SpawnSchedule implements Runnable
	{
		private final int bossId;

		public SpawnSchedule(int npcId)
		{
			bossId = npcId;
		}

		@Override
		public void run()
		{
			L2RaidBossInstance raidboss = null;

			raidboss = bossId == 25328 ? DayNightSpawnManager.getInstance().handleBoss(_spawns.get(bossId)) : (L2RaidBossInstance) _spawns.get(bossId).doSpawn();

			if(raidboss != null)
			{
				raidboss.setRaidStatus(StatusEnum.ALIVE);

				StatsSet info = new StatsSet();
				info.set("currentHP", raidboss.getCurrentHp());
				info.set("currentMP", raidboss.getCurrentMp());
				info.set("respawnTime", 0L);

				_storedInfo.put(bossId, info);

				_log.log(Level.INFO, "Spawning Raid Boss " + raidboss.getName());

				_bosses.put(bossId, raidboss);
			}

			_schedules.remove(bossId);
		}
	}

	private static class SingletonHolder
	{
		protected static final RaidBossSpawnManager _instance = new RaidBossSpawnManager();
	}
}