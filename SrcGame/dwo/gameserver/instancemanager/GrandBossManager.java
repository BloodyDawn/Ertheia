package dwo.gameserver.instancemanager;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.util.arrays.L2FastList;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author DaRkRaGe
 *         Revised by Emperorc
 */
public class GrandBossManager
{
    /*
      * =========================================================
      * This class handles all Grand Bosses:
      * <ul>
      * <li>25333-25338 Anakazel</li>
      * <li>29001 Queen Ant</li>
      * <li>29006 Core</li>
      * <li>29014 Orfen</li>
      * <li>29019 Antharas</li>
      * <li>29020 Baium</li>
      * <li>29028 Valakas</li>
      * <li>29046-29047 Scarlet van Halisha</li>
      * </ul>
      * It handles the saving of hp, mp, location, and status
      * of all Grand Bosses. It also manages the zones associated
      * with the Grand Bosses.
      * NOTE: The current version does NOT spawn the Grand Bosses,
      * it just stores and retrieves the values on reboot/startup,
      * for AI scripts to utilize as needed.
      */

	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";

	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";

	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";

	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";

	protected static Logger _log = LogManager.getLogger(GrandBossManager.class);

	protected static TIntObjectHashMap<L2GrandBossInstance> _bosses;

	protected static TIntObjectHashMap<StatsSet> _storedInfo;

	private TIntIntHashMap _bossStatus;

	private L2FastList<L2BossZone> _zones;

	private GrandBossManager()
	{
		_log.log(Level.INFO, "Initializing GrandBossManager...");
		init();
	}

	public static GrandBossManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void init()
	{
		_zones = new L2FastList<>();

		_bosses = new TIntObjectHashMap<>();
		_storedInfo = new TIntObjectHashMap<>();
		_bossStatus = new TIntIntHashMap();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
			rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP"); // jython doesn't recognize doubles
				int true_HP = (int) HP; // so use java's ability to type cast
				info.set("currentHP", true_HP); // to convert double to int
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				int status = rset.getInt("status");
				_bossStatus.put(bossId, status);
				_storedInfo.put(bossId, info);
				_log.log(Level.INFO, "GrandBossManager: " + NpcTable.getInstance().getTemplate(bossId).getName() + '(' + bossId + ") status is " + status + '.');
				if(status > 0)
				{
					_log.log(Level.INFO, "GrandBossManager: Next spawn date of " + NpcTable.getInstance().getTemplate(bossId).getName() + " is " + new Date(info.getLong("respawn_time")) + '.');
				}

				info = null;
			}
			_log.log(Level.INFO, "GrandBossManager: Loaded " + _storedInfo.size() + " Instances");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "GrandBossManager: Could not load grandboss_data table: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "GrandBossManager: Error while initializing: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/*
	 * Функции зон
	 */
	public void initZones()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		TIntObjectHashMap<L2FastList<Integer>> zones = new TIntObjectHashMap<>();

		if(_zones == null)
		{
			_log.log(Level.WARN, "GrandBossManager: Could not read Grand Boss zone data");
			return;
		}

		for(L2BossZone zone : _zones)
		{
			if(zone == null)
			{
				continue;
			}
			zones.put(zone.getId(), new L2FastList<>());
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("player_id");
				int zone_id = rset.getInt("zone");
				zones.get(zone_id).add(id);
			}

			_log.log(Level.INFO, "GrandBossManager: Initialized " + _zones.size() + " Grand Boss Zones");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "GrandBossManager: Could not load grandboss_list table: " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while initializing GrandBoss zones: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		for(L2BossZone zone : _zones)
		{
			if(zone == null)
			{
				continue;
			}
			zone.setAllowedPlayers(zones.get(zone.getId()));
		}

		zones.clear();
	}

	/**
	 * Добавить зону босса
	 * @param zone зона босса
	 */
	public void addZone(L2BossZone zone)
	{
		if(_zones != null)
		{
			_zones.add(zone);
		}
	}

	/**
	 * @param character персонаж
	 * @return если игрок находится в зоне боса, возвращает её
	 */
	public L2BossZone getZone(L2Character character)
	{
		if(_zones != null)
		{
			for(L2BossZone temp : _zones)
			{
				if(temp.isCharacterInZone(character))
				{
					return temp;
				}
			}
		}
		return null;
	}

	/**
	 * @param x X
	 * @param y Y
	 * @param z Z
	 * @return возвращает зону, по данным координатам (если существует)
	 */
	public L2BossZone getZone(int x, int y, int z)
	{
		if(_zones != null)
		{
			for(L2BossZone temp : _zones)
			{
				if(temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		return null;
	}

	/**
	 * @param zoneType тип зоны
	 * @param obj персонаж-объект
	 * @return true, если обьект находится в зоне заданного типа
	 */
	public boolean checkIfInZone(String zoneType, L2Object obj)
	{
		L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if(temp == null)
		{
			return false;
		}

		return temp.getName().equalsIgnoreCase(zoneType);
	}

	public boolean checkIfInZone(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}
		L2BossZone temp = getZone(player.getX(), player.getY(), player.getZ());
		return temp != null;

	}

    /*
     * Разное
     */

	/**
	 * @param bossId ID Грандбосса
	 * @return статус Грандбосса
	 */
	public int getBossStatus(int bossId)
	{
		return _bossStatus.get(bossId);
	}

	/**
	 * Устанавливает заданный статус Грандбосса
	 * @param bossId ID Грандбосса
	 * @param status статус
	 */
	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		_log.log(Level.INFO, getClass().getSimpleName() + ": Updated " + NpcTable.getInstance().getTemplate(bossId).getName() + '(' + bossId + ") status to " + status);
		updateDb(bossId, true);
	}

	/**
	 * Добавляет инстанс Грандбосса в лист боссов
	 * @param boss грандбосс
	 */
	public void addBoss(L2GrandBossInstance boss)
	{
		if(boss != null)
		{
			_bosses.put(boss.getNpcId(), boss);
		}
	}

	/**
	 * @param bossId ID Грандбосса
	 * @return инстанс Грандбосса
	 */
	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}

	/**
	 * @param bossId ID Грандбосса
	 * @return информацию о Грандбоссе
	 */
	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}

	/**
	 * Устанавливает дополнительную информацию о Грандбоссе
	 * @param bossId ID Грандбосса
	 * @param info StatsSet-информация
	 */
	public void setStatsSet(int bossId, StatsSet info)
	{
		_storedInfo.put(bossId, info);
		updateDb(bossId, false);
	}

	/**
	 * Сохранение в базу всей информации по Грандбоссам
	 */
	private void storeToDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
			for(L2BossZone zone : _zones)
			{
				if(zone == null)
				{
					continue;
				}
				Integer id = zone.getId();
				L2FastList<Integer> list = zone.getAllowedPlayers();
				if(list == null || list.isEmpty())
				{
					continue;
				}
				for(Integer player : list)
				{
					statement.setInt(1, player);
					statement.setInt(2, id);
					statement.executeUpdate();
					statement.clearParameters();
				}
			}
			DatabaseUtils.closeStatement(statement);

			for(Integer bossId : _storedInfo.keys())
			{
				L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if(boss == null || info == null)
				{
					statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
					statement.setInt(1, _bossStatus.get(bossId));
					statement.setInt(2, bossId);
				}
				else
				{
					statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
					statement.setInt(1, boss.getX());
					statement.setInt(2, boss.getY());
					statement.setInt(3, boss.getZ());
					statement.setInt(4, boss.getHeading());
					statement.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if(boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					statement.setDouble(6, hp);
					statement.setDouble(7, mp);
					statement.setInt(8, _bossStatus.get(bossId));
					statement.setInt(9, bossId);
				}
				statement.executeUpdate();
				statement.clearParameters();
				DatabaseUtils.closeStatement(statement);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "GrandBossManager: Couldn't store grandbosses to database:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	/**
	 * Актуализация текущей информацией с базой по заданному Грандбоссу
	 * @param bossId ID Грандбосса
	 * @param statusOnly сохранять только статус?
	 */
	private void updateDb(int bossId, boolean statusOnly)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			L2GrandBossInstance boss = _bosses.get(bossId);
			StatsSet info = _storedInfo.get(bossId);

			if(statusOnly || boss == null || info == null)
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
				statement.setInt(1, _bossStatus.get(bossId));
				statement.setInt(2, bossId);
			}
			else
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
				statement.setInt(1, boss.getX());
				statement.setInt(2, boss.getY());
				statement.setInt(3, boss.getZ());
				statement.setInt(4, boss.getHeading());
				statement.setLong(5, info.getLong("respawn_time"));
				double hp = boss.getCurrentHp();
				double mp = boss.getCurrentMp();
				if(boss.isDead())
				{
					hp = boss.getMaxHp();
					mp = boss.getMaxMp();
				}
				statement.setDouble(6, hp);
				statement.setDouble(7, mp);
				statement.setInt(8, _bossStatus.get(bossId));
				statement.setInt(9, bossId);
			}
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "GrandBossManager: Couldn't update grandbosses to database:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Сохраняет всю информацию о Грандбоссах и вычищает всю информацию
	 * о них из памяти, включая все таймеры.
	 */
	public void cleanUp()
	{
		storeToDb();

		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
	}

	public L2FastList<L2BossZone> getZones()
	{
		return _zones;
	}

	private static class SingletonHolder
	{
		protected static final GrandBossManager _instance = new GrandBossManager();
	}
}
