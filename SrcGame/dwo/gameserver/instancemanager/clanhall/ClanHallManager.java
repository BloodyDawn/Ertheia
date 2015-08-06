package dwo.gameserver.instancemanager.clanhall;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.AuctionManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.ClanHallAuctionEngine;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.zone.type.L2ClanHallZone;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Map;

/**
 * @author Steuf
 */

public class ClanHallManager
{
	protected static final Logger _log = LogManager.getLogger(ClanHallManager.class);
	private static Map<Integer, ClanHall> _allClanHalls = new FastMap<>();
	private Map<Integer, AuctionableHall> _clanHall;
	private Map<Integer, AuctionableHall> _freeClanHall;
	private Map<Integer, AuctionableHall> _allAuctionableClanHalls;
	private boolean _loaded;

	private ClanHallManager()
	{
		_log.log(Level.INFO, "ClanHallManager: Initializing...");
		_clanHall = new FastMap<>();
		_freeClanHall = new FastMap<>();
		_allAuctionableClanHalls = new FastMap<>();
		load();
	}

	public static ClanHallManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * @return Map со всеми Клаехоллами
	 */
	public static Map<Integer, ClanHall> getAllClanHalls()
	{
		return _allClanHalls;
	}

	/**
	 * Добавляем Кланхолл в общий Map
	 * @param hall добавляемый ClanHall
	 */
	public static void addClanHall(ClanHall hall)
	{
		_allClanHalls.put(hall.getId(), hall);
	}

	public boolean loaded()
	{
		return _loaded;
	}

	/**
	 * Грузим все Кланхоллы из ДП
	 */
	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			int id;
			int ownerId;
			int lease;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rs = statement.executeQuery();
			while(rs.next())
			{
				StatsSet set = new StatsSet();

				id = rs.getInt("id");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");

				set.set("id", id);
				set.set("name", rs.getString("name"));
				set.set("ownerId", ownerId);
				set.set("lease", lease);
				set.set("desc", rs.getString("desc"));
				set.set("location", rs.getString("location"));
				set.set("paidUntil", rs.getLong("paidUntil"));
				set.set("grade", rs.getInt("Grade"));
				set.set("paid", rs.getBoolean("paid"));
				AuctionableHall ch = new AuctionableHall(set);
				_allAuctionableClanHalls.put(id, ch);
				addClanHall(ch);

				if(ch.getOwnerId() > 0)
				{
					_clanHall.put(id, ch);
					continue;
				}
				_freeClanHall.put(id, ch);

				ClanHallAuctionEngine auc = AuctionManager.getInstance().getAuction(id);
				if(auc == null && lease > 0)
				{
					AuctionManager.getInstance().initNPC(id);
				}
			}

			_log.log(Level.INFO, "ClanHallManager: Loaded " + _clanHall.size() + " clan halls");
			_log.log(Level.INFO, "ClanHallManager: Loaded " + _freeClanHall.size() + " free clan halls");
			_loaded = true;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "ClanHallManager: Exception in ClanHallManager.load(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * @return Map со всеми свободными Кланхоллами
	 */
	public Map<Integer, AuctionableHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}

	/**
	 * @return Map со всеми Кланхоллами, имеющими владельца
	 */
	public Map<Integer, AuctionableHall> getClanHalls()
	{
		return _clanHall;
	}

	/**
	 * @return Map со всеми Auctionable кланхоллами
	 */
	public Map<Integer, AuctionableHall> getAllAuctionableClanHalls()
	{
		return _allAuctionableClanHalls;
	}

	/**
	 * @param chId ID Кланхолла
	 * @return Является-ли Кланхолл свободным
	 */
	public boolean isFree(int chId)
	{
		return _freeClanHall.containsKey(chId);
	}

	/**
	 * Освобождает Кланхолл
	 * @param chId ID Кланхолла
	 */
	public void setFree(int chId)
	{
		synchronized(this)
		{
			_freeClanHall.put(chId, _clanHall.get(chId));
			ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setClanhallId(0);
			_freeClanHall.get(chId).free();
			_clanHall.remove(chId);
		}
	}

	/**
	 * Задает владельца Кланхоллу
	 * @param chId ID Кланхолла
	 * @param clan L2Clan владельца
	 */
	public void setOwner(int chId, L2Clan clan)
	{
		synchronized(this)
		{
			if(_clanHall.containsKey(chId))
			{
				_clanHall.get(chId).free();
			}
			else
			{
				_clanHall.put(chId, _freeClanHall.get(chId));
				_freeClanHall.remove(chId);
			}
			ClanTable.getInstance().getClan(clan.getClanId()).setClanhallId(chId);
			_clanHall.get(chId).setOwner(clan);
		}
	}

	/**
	 * @param clanHallId ID кланхолла
	 * @return Кланхолл с заданным clanHallId
	 */
	public ClanHall getClanHallById(int clanHallId)
	{
		return _allClanHalls.get(clanHallId);
	}

	/**
	 * @param clanHallId ID Auctionable Кланхолла
	 * @return Auctionable Кланхолл с заданным clanHallId
	 */
	public AuctionableHall getAuctionableHallById(int clanHallId)
	{
		return _allAuctionableClanHalls.get(clanHallId);
	}

	/**
	 * @param x координата по X
	 * @param y координата по Y
	 * @param z координата по Z
	 * @return Кланхолл расположенный по заданным координатам
	 */
	public ClanHall getClanHall(int x, int y, int z)
	{
		for(ClanHall temp : _allClanHalls.values())
		{
			if(temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}
		return null;
	}

	public ClanHall getClanHall(L2Object activeObject)
	{
		return getClanHall(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	/**
	 * @param x координата по X
	 * @param y координата по Y
	 * @param maxDist максимальная дистаниция до Кланхолла
	 * @return ближайший Auctionable Кланхолл
	 */
	public AuctionableHall getNearbyClanHall(int x, int y, int maxDist)
	{
		L2ClanHallZone zone = null;

		for(Map.Entry<Integer, AuctionableHall> ch : _clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if(zone != null && zone.getDistanceToZone(x, y) < maxDist)
			{
				return ch.getValue();
			}
		}
		for(Map.Entry<Integer, AuctionableHall> ch : _freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if(zone != null && zone.getDistanceToZone(x, y) < maxDist)
			{
				return ch.getValue();
			}
		}
		return null;
	}

	/**
	 * @param x координата по X
	 * @param y координата по Y
	 * @param maxDist максимальная дистаниция до Кланхолла
	 * @return ближайший любой Кланхолл
	 */
	public ClanHall getNearbyAbstractHall(int x, int y, int maxDist)
	{
		L2ClanHallZone zone = null;
		for(Map.Entry<Integer, ClanHall> ch : _allClanHalls.entrySet())
		{
			zone = ch.getValue().getZone();
			if(zone != null && zone.getDistanceToZone(x, y) < maxDist)
			{
				return ch.getValue();
			}
		}
		return null;
	}

	/**
	 * @param clan L2Clan
	 * @return возвращает КХ, принадлежащий указанному клану
	 */
	public AuctionableHall getClanHallByOwner(L2Clan clan)
	{
		for(Map.Entry<Integer, AuctionableHall> ch : _clanHall.entrySet())
		{
			if(clan.getClanId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		return null;
	}

	public ClanHall getAbstractHallByOwner(L2Clan clan)
	{
		// Separate loops to avoid iterating over free clan halls
		for(Map.Entry<Integer, AuctionableHall> ch : _clanHall.entrySet())
		{
			if(clan.getClanId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		for(Map.Entry<Integer, ClanHallSiegable> ch : ClanHallSiegeManager.getInstance().getConquerableHalls().entrySet())
		{
			if(clan.getClanId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		return null;
	}

	private static class SingletonHolder
	{
		protected static final ClanHallManager _instance = new ClanHallManager();
	}
}