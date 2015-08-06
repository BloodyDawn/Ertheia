package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author _DS_, GKR
 */

public class HellboundManager
{
	private static final Logger _log = LogManager.getLogger(HellboundManager.class);

	private static final String LOAD_SPAWNS = "SELECT npc_templateid, locx, locy, locz, heading, " +
		"respawn_delay, respawn_random, min_hellbound_level, " +
		"max_hellbound_level FROM hellbound_spawnlist ORDER BY npc_templateid";
	private final List<HellboundSpawn> _population;
	private int _level;
	private int _trust;
	private int _maxTrust;
	private int _minTrust;
	private ScheduledFuture<?> _engine;

	private HellboundManager()
	{
		_population = new FastList<>();

		loadData();
		loadSpawns();
	}

	public static HellboundManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(int lvl)
	{
		_level = lvl;
	}

	public void updateTrust(int t, boolean useRates)
	{
		synchronized(this)
		{
			if(isLocked())
			{
				return;
			}

			int reward = t;
			if(useRates)
			{
				reward = (int) (t > 0 ? Config.RATE_HB_TRUST_INCREASE * t : Config.RATE_HB_TRUST_DECREASE * t);
			}

			int trust = Math.max(_trust + reward, _minTrust);
			_trust = _maxTrust > 0 ? Math.min(trust, _maxTrust) : trust;
		}
	}

	public int getTrust()
	{
		return _trust;
	}

	public int getMaxTrust()
	{
		return _maxTrust;
	}

	public void setMaxTrust(int trust)
	{
		_maxTrust = trust;
		if(_maxTrust > 0 && _trust > _maxTrust)
		{
			_trust = _maxTrust;
		}
	}

	public int getMinTrust()
	{
		return _minTrust;
	}

	public void setMinTrust(int trust)
	{
		_minTrust = trust;

		if(_trust >= _maxTrust)
		{
			_trust = _minTrust;
		}
	}

	/**
	 * @return {@code true} если хеллбаунд закрыт
	 */
	public boolean isLocked()
	{
		return _level == 0;
	}

	public void unlock()
	{
		if(_level == 0)
		{
			_level = 1;
		}
	}

	public void registerEngine(Runnable r, int interval)
	{
		if(_engine != null)
		{
			_engine.cancel(false);
		}
		_engine = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(r, interval, interval);
	}

	public void doSpawn()
	{
		int added = 0;
		int deleted = 0;
		for(HellboundSpawn spawnDat : _population)
		{
			try
			{
				if(spawnDat == null)
				{
					continue;
				}

				L2Npc npc = spawnDat.getLastSpawn();
				if(_level < spawnDat.getMinLvl() || _level > spawnDat.getMaxLvl())
				{
					// НПЦ должен быть удален
					spawnDat.stopRespawn();

					if(npc != null && npc.isVisible())
					{
						npc.getLocationController().delete();
						deleted++;
					}
				}
				else
				{
					// НПЦ должен быть добавлен
					spawnDat.startRespawn();
					npc = spawnDat.getLastSpawn();
					if(npc == null)
					{
						npc = spawnDat.doSpawn();
						added++;
					}
					else
					{
						if(npc.isDecayed())
						{
							npc.setDecayed(false);
						}
						if(npc.isDead())
						{
							npc.doRevive();
						}
						if(!npc.isVisible())
						{
							added++;
						}

						npc.setCurrentHp(npc.getMaxHp());
						npc.setCurrentMp(npc.getMaxMp());
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, e);
			}
		}

		if(added > 0)
		{
			_log.log(Level.INFO, "HellboundManager: Spawned " + added + " NPCs.");
		}
		if(deleted > 0)
		{
			_log.log(Level.INFO, "HellboundManager: Removed " + deleted + " NPCs.");
		}
	}

	public void cleanUp()
	{
		saveData();

		if(_engine != null)
		{
			_engine.cancel(true);
			_engine = null;
		}
		_population.clear();
	}

	private void loadData()
	{
		if(GlobalVariablesManager.getInstance().isVariableStored("HBLevel"))
		{
			_level = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("HBLevel"));
			_trust = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("HBTrust"));
		}
		else
		{
			saveData();
		}
	}

	public void saveData()
	{
		GlobalVariablesManager.getInstance().storeVariable("HBLevel", String.valueOf(_level));
		GlobalVariablesManager.getInstance().storeVariable("HBTrust", String.valueOf(_trust));
	}

	private void loadSpawns()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOAD_SPAWNS);
			rset = statement.executeQuery();

			HellboundSpawn spawnDat;
			L2NpcTemplate template;
			while(rset.next())
			{
				template = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if(template != null)
				{
					spawnDat = new HellboundSpawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					spawnDat.setRespawnMinDelay(0);
					spawnDat.setRespawnMaxDelay(0);
					int respawnRandom = rset.getInt("respawn_random");
					if(respawnRandom > 0) // Random respawn time, if needed
					{
						spawnDat.setRespawnMinDelay(Math.max(rset.getInt("respawn_delay") - respawnRandom, 1));
						spawnDat.setRespawnMaxDelay(rset.getInt("respawn_delay") + respawnRandom);
					}
					spawnDat.setMinLvl(rset.getInt("min_hellbound_level"));
					spawnDat.setMaxLvl(rset.getInt("max_hellbound_level"));

					// _population.put(spawnDat, null);
					_population.add(spawnDat);
					SpawnTable.getInstance().addNewSpawn(spawnDat);
				}
				else
				{
					_log.log(Level.WARN, "HellboundManager: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + '.');
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "HellboundManager: problem while loading spawns: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.log(Level.INFO, "HellboundManager: Loaded " + _population.size() + " npc spawn locations.");
	}

	public static class HellboundSpawn extends L2Spawn
	{
		private int _respawnDelay;

		private int _minLvl;
		private int _maxLvl;

		public HellboundSpawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
		{
			super(mobTemplate);
		}

		public int getMinLvl()
		{
			return _minLvl;
		}

		public void setMinLvl(int lvl)
		{
			_minLvl = lvl;
		}

		public int getMaxLvl()
		{
			return _maxLvl;
		}

		public void setMaxLvl(int lvl)
		{
			_maxLvl = lvl;
		}

		@Override
		public int getRespawnDelay()
		{
			return _respawnDelay;
		}

		/**
		 * @param i задердка перед респауном (в секундах)
		 */
		@Override
		public void setRespawnDelay(int i)
		{
			_respawnDelay = i * 1000;

			super.setRespawnDelay(i);
		}

		@Override
		public void decreaseCount(L2Npc oldNpc)
		{
			if(_respawnDelay <= 0)
			{
				stopRespawn();
			}
			else if(getRespawnMaxDelay() > getRespawnMinDelay())
			{
				setRespawnDelay(Rnd.get(getRespawnMinDelay(), getRespawnMaxDelay()));
			}
			super.decreaseCount(oldNpc);
		}
	}

	private static class SingletonHolder
	{
		protected static final HellboundManager _instance = new HellboundManager();
	}
}