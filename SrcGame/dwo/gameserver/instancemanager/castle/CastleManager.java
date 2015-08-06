package dwo.gameserver.instancemanager.castle;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CastleManager
{
	protected static final Logger _log = LogManager.getLogger(CastleManager.class);
	private static final List<Castle> _castles = new FastList<>();
	private static final Map<Integer, int[]> _castlesForts = new HashMap<>();
	private static final int[] _castleCirclets = {0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183};
	private static final int[] _castleCloaks = {34925, 34926, 34996, 34997};
	int _castleId = 1; // from this castle

	private CastleManager()
	{
		_log.log(Level.INFO, "CastleManager: Initializing...");
		load();
		_log.log(Level.INFO, "CastleManager: Loaded " + _castles.size() + " castles.");
	}

	public static CastleManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void load()
	{
		_castles.clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM castle ORDER BY id");
			rs = statement.executeQuery();

			while(rs.next())
			{
				_castles.add(new Castle(rs.getInt("id")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleManager: Exception in loadCastleData(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}

		// Делаем карту поиска списка фортов, которые относятся к тому или иному замку
		_castlesForts.put(1, new int[]{101, 102, 112, 113});        // Глудио
		_castlesForts.put(2, new int[]{103, 112, 114, 115});        // Дион
		_castlesForts.put(3, new int[]{104, 114, 116, 118, 119});    // Гиран
		_castlesForts.put(4, new int[]{105, 113, 115, 116, 117});    // Орен
		_castlesForts.put(5, new int[]{106, 107, 117, 118});        // Аден
		_castlesForts.put(6, new int[]{108, 119});                    // Иннадрил
		_castlesForts.put(7, new int[]{109, 117, 120});            // Годдарт
		_castlesForts.put(8, new int[]{110, 120, 121});            // Руна
		_castlesForts.put(9, new int[]{111, 121});                    // Штуттгарт
	}

	public int findNearestCastleIndex(L2Object obj)
	{
		return findNearestCastleIndex(obj, Long.MAX_VALUE);
	}

	public int findNearestCastleIndex(L2Object obj, long maxDistance)
	{
		int index = getCastleIndex(obj);
		if(index < 0)
		{
			double distance;
			Castle castle;
			for(int i = 0; i < _castles.size(); i++)
			{
				castle = _castles.get(i);
				if(castle == null)
				{
					continue;
				}
				distance = castle.getDistance(obj);
				if(maxDistance > distance)
				{
					maxDistance = (long) distance;
					index = i;
				}
			}
		}
		return index;
	}

	public Castle getCastleById(int castleId)
	{
		for(Castle temp : _castles)
		{
			if(temp.getCastleId() == castleId)
			{
				return temp;
			}
		}
		return null;
	}

	public Castle getCastleByOwner(L2Clan clan)
	{
		for(Castle temp : _castles)
		{
			if(temp.getOwnerId() == clan.getClanId())
			{
				return temp;
			}
		}
		return null;
	}

	public Castle getCastle(String name)
	{
		for(Castle temp : _castles)
		{
			if(temp.getName().equalsIgnoreCase(name.trim()))
			{
				return temp;
			}
		}
		return null;
	}

	public Castle getCastle(int x, int y, int z)
	{
		for(Castle temp : _castles)
		{
			if(temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}
		return null;
	}

	public Castle getCastle(Location loc)
	{
		for(Castle temp : _castles)
		{
			if(temp.checkIfInZone(loc.getX(), loc.getY(), loc.getZ()))
			{
				return temp;
			}
		}
		return null;
	}

	public Castle getCastle(int castleId)
	{
		for(Castle castle : _castles)
		{
			if(castle.getCastleId() == castleId)
			{
				return castle;
			}
		}
		return null;
	}

	public Castle getCastle(L2Object activeObject)
	{
		return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public int getCastleIndex(int castleId)
	{
		Castle castle;
		for(int i = 0; i < _castles.size(); i++)
		{
			castle = _castles.get(i);
			if(castle != null && castle.getCastleId() == castleId)
			{
				return i;
			}
		}
		return -1;
	}

	public int getCastleIndex(L2Object activeObject)
	{
		return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public int getCastleIndex(int x, int y, int z)
	{
		Castle castle;
		for(int i = 0; i < _castles.size(); i++)
		{
			castle = _castles.get(i);
			if(castle != null && castle.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}

	public List<Castle> getCastles()
	{
		return _castles;
	}

	public int getCirclet()
	{
		return getCircletByCastleId(_castleId);
	}

	public int getCircletByCastleId(int castleId)
	{
		if(castleId > 0 && castleId < 10)
		{
			return _castleCirclets[castleId];
		}

		return 0;
	}

	// remove this castle's circlets from the clan
	public void removeCirclet(L2Clan clan, int castleId)
	{
		for(L2ClanMember member : clan.getMembers())
		{
			removeCirclet(member, castleId);
		}
	}

	public void removeCirclet(L2ClanMember member, int castleId)
	{
		if(member == null)
		{
			return;
		}
		L2PcInstance player = member.getPlayerInstance();
		int circletId = getCircletByCastleId(castleId);

		if(circletId != 0)
		{
			// online-player circlet removal
			if(player != null)
			{
				try
				{
					L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
					if(circlet != null)
					{
						if(circlet.isEquipped())
						{
							player.getInventory().unEquipItemInSlot(circlet.getLocationSlot());
						}
						player.destroyItemByItemId(ProcessType.CASTLE, circletId, 1, player, true);
					}
					return;
				}
				catch(NullPointerException e)
				{
					// continue removing offline
				}
			}
			// else offline-player circlet removal
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
				statement.setInt(1, member.getObjectId());
				statement.setInt(2, circletId);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Failed to remove castle circlets offline for player " + member.getName() + ": " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	/***
	 * Удаление плащей у игроков, которые потеряли замок
	 * @param clan клан, который потерял замок
	 */
	public void removeCloaks(L2Clan clan)
	{
		if(clan == null)
		{
			return;
		}

		for(L2ClanMember member : clan.getMembers())
		{
			removeCloak(member);
		}
	}

	public void removeCloak(L2ClanMember member)
	{
		if(member.isOnline())
		{
			L2PcInstance player = member.getPlayerInstance();
			try
			{
				L2ItemInstance cloak;
				for(int cloakId : _castleCloaks)
				{
					cloak = player.getInventory().getItemByItemId(cloakId);
					if(cloak != null)
					{
						if(cloak.isEquipped())
						{
							player.getInventory().unEquipItemInSlot(cloak.getLocationSlot());
						}
						player.destroyItemByItemId(ProcessType.CASTLE, cloakId, 1, player, true);
					}
				}
			}
			catch(NullPointerException e)
			{
				// continue removing offline
			}
		}
		else
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
				for(int cloakId : _castleCloaks)
				{
					statement.setInt(1, member.getObjectId());
					statement.setInt(2, cloakId);
					statement.execute();
					statement.clearParameters();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Failed to remove cloaks offline for player " + member.getName() + ": " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public void spawnDoors()
	{
		for(Castle castle : _castles)
		{
			castle.spawnDoors();
		}
	}

	/***
	 * @param castleId ID замка
	 * @return список ID фортов, которые находятся на территории указанного замка
	 */
	public int[] getCastleForts(int castleId)
	{
		return _castlesForts.get(castleId);
	}

	private static class SingletonHolder
	{
		protected static final CastleManager _instance = new CastleManager();
	}
}