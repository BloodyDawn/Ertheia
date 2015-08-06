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
package dwo.gameserver.instancemanager;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kerberos
 *         JIV update 24.8.10
 */

public class RaidBossPointsManager
{
	private static final Logger _log = LogManager.getLogger(RaidBossPointsManager.class);
	private final Comparator<Entry<Integer, Integer>> _comparator = (entry, entry1) -> entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;
	private FastMap<Integer, TIntIntHashMap> _list;

	public RaidBossPointsManager()
	{
		init();
	}

	public static RaidBossPointsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void init()
	{
		_list = new FastMap<>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `charId`,`boss_id`,`points` FROM `character_raid_points`");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int charId = rset.getInt("charId");
				int bossId = rset.getInt("boss_id");
				int points = rset.getInt("points");
				TIntIntHashMap values = _list.get(charId);
				if(values == null)
				{
					values = new TIntIntHashMap();
				}
				values.put(bossId, points);
				_list.put(charId, values);
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _list.size() + " Characters Raid Points.");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "RaidPointsManager: Couldnt load raid points ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void updatePointsInDB(L2PcInstance player, int raidId, int points)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, raidId);
			statement.setInt(3, points);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not update char raid points:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void addPoints(L2PcInstance player, int bossId, int points)
	{
		int ownerId = player.getObjectId();
		TIntIntHashMap tmpPoint = _list.get(ownerId);
		if(tmpPoint == null)
		{
			tmpPoint = new TIntIntHashMap();
			tmpPoint.put(bossId, points);
			updatePointsInDB(player, bossId, points);
		}
		else
		{
			int currentPoins = tmpPoint.containsKey(bossId) ? tmpPoint.get(bossId) : 0;
			currentPoins += points;
			tmpPoint.put(bossId, currentPoins);
			updatePointsInDB(player, bossId, currentPoins);
		}
		_list.put(ownerId, tmpPoint);
	}

	public int getPointsByOwnerId(int ownerId)
	{
		TIntIntHashMap tmpPoint;
		tmpPoint = _list.get(ownerId);
		int totalPoints = 0;

		if(tmpPoint == null || tmpPoint.isEmpty())
		{
			return 0;
		}

		for(int points : tmpPoint.values())
		{
			totalPoints += points;
		}
		return totalPoints;
	}

	public TIntIntHashMap getList(L2PcInstance player)
	{
		return _list.get(player.getObjectId());
	}

	public void cleanUp()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE from character_raid_points WHERE charId > 0");
			statement.executeUpdate();
			_list.clear();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not clean raid points: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int calculateRanking(int playerObjId)
	{
		Map<Integer, Integer> rank = getRankList();
		if(rank.containsKey(playerObjId))
		{
			return rank.get(playerObjId);
		}
		return 0;
	}

	public Map<Integer, Integer> getRankList()
	{
		Map<Integer, Integer> tmpRanking = new FastMap<>();
		Map<Integer, Integer> tmpPoints = new FastMap<>();

		for(int ownerId : _list.keySet())
		{
			int totalPoints = getPointsByOwnerId(ownerId);
			if(totalPoints != 0)
			{
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		List<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());

		Collections.sort(list, _comparator);

		int ranking = 1;
		for(Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}

		return tmpRanking;
	}

	private static class SingletonHolder
	{
		protected static final RaidBossPointsManager _instance = new RaidBossPointsManager();
	}
}