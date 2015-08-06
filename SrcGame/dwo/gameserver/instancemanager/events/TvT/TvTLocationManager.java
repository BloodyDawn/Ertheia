package dwo.gameserver.instancemanager.events.TvT;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TvTLocationManager
{
	public static final int TEAM_BLUE = 0;
	public static final int TEAM_RED = 1;
	protected static final Logger _log = LogManager.getLogger(TvTLocationManager.class);
	private static final String DELETE_LOCS = "TRUNCATE TABLE mods_tvt_locs";
	private static final String SELECT_LOCS = "SELECT * FROM mods_tvt_locs";
	private static final String INSERT_LOCS = "INSERT INTO mods_tvt_locs (locationName, locationId, teamId, x, y, z) VALUES (?, ?, ?, ?, ?, ?)";
	private static Map<Integer, TvTLocation> _teamLocs = new FastMap<Integer, TvTLocation>().shared();
	private final Map<Integer, L2Npc> _debugNpcs = new FastMap<>();
	private int _lastId;
	private int _lastMap;

	public TvTLocationManager()
	{
		load();
	}

	public static TvTLocationManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		_teamLocs.clear();

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_LOCS);
			rset = statement.executeQuery();
			while(rset.next())
			{
				Location loc = new Location(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				loc.setId(rset.getInt("id"));
				int teamId = rset.getInt("teamId");

				int locationId = rset.getInt("locationId");
				String locationName = rset.getString("locationName");

				TvTLocation tvtloc;

				_lastId = rset.getInt("id");

				if(_teamLocs.containsKey(locationId))
				{
					tvtloc = _teamLocs.get(locationId);
				}
				else
				{
					tvtloc = new TvTLocation(locationId, locationName);
					_teamLocs.put(tvtloc.getId(), tvtloc);
				}

				tvtloc.addLocationToTeam(teamId, loc);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void clear(boolean clear)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_LOCS);
			statement.execute();
			if(clear)
			{
				_teamLocs.clear();
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void clearMap()
	{
		_teamLocs.clear();
	}

	public void save()
	{
		clear(false); // else we will duplicate them..

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			if(!_teamLocs.isEmpty())
			{
				for(Entry<Integer, TvTLocation> entry : _teamLocs.entrySet())
				{
					statement = con.prepareStatement(INSERT_LOCS);
					for(Location loc : entry.getValue().getLocationsForTeam(TEAM_BLUE))
					{
						statement.setString(1, entry.getValue().getName());
						statement.setInt(2, entry.getValue().getId());
						statement.setInt(3, TEAM_BLUE);
						statement.setInt(4, loc.getX());
						statement.setInt(5, loc.getY());
						statement.setInt(6, loc.getZ());
						statement.execute();
						statement.clearParameters();
					}

					for(Location loc : entry.getValue().getLocationsForTeam(TEAM_RED))
					{
						statement.setString(1, entry.getValue().getName());
						statement.setInt(2, entry.getValue().getId());
						statement.setInt(3, TEAM_RED);
						statement.setInt(4, loc.getX());
						statement.setInt(5, loc.getY());
						statement.setInt(6, loc.getZ());
						statement.execute();
						statement.clearParameters();
					}
				}
			}

			_log.log(Level.INFO, "TvTEvent saved: " + _teamLocs.size() + " locations");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public TvTLocation getLocation(int id)
	{
		return _teamLocs.get(id);
	}

	public Map<Integer, TvTLocation> getLocations()
	{
		return _teamLocs;
	}

	public TvTLocation createNewLocation(String name)
	{
		int lastId = 0;
		for(int id : _teamLocs.keySet())
		{
			if(id > lastId)
			{
				lastId = id;
			}
		}
		TvTLocation loc = new TvTLocation(lastId + 1, name);
		_teamLocs.put(lastId + 1, loc);
		return loc;
	}

	public void removeLocation(int id)
	{
		_teamLocs.remove(id);
	}

	public int generateRandomMap()
	{
		int[] maps = new int[_teamLocs.size()];
		int i = 0;
		for(Integer map : _teamLocs.keySet())
		{
			maps[i++] = map;
		}
		int posId = _lastMap;
		while(posId == _lastMap)
		{
			posId = maps[Rnd.get(maps.length)];
		}
		return posId;
	}

	public int getLastId()
	{
		return _lastId;
	}

	public Map<Integer, L2Npc> getDebugNpcs()
	{
		return _debugNpcs;
	}

	public static class TvTLocation
	{
		private final int _id;
		private final String _name;
		private final Map<Integer, List<Location>> _locations;
		private int _lastId;

		public TvTLocation(int id, String name)
		{
			_id = id;
			_name = name;
			_locations = new FastMap<Integer, List<Location>>().shared();
			_locations.put(TEAM_BLUE, new FastList<Location>().shared());
			_locations.put(TEAM_RED, new FastList<Location>().shared());
		}

		public int getId()
		{
			return _id;
		}

		public String getName()
		{
			return _name;
		}

		public List<Location> getLocations()
		{
			List<Location> temp = new FastList<Location>().shared();
			temp.addAll(_locations.get(TEAM_BLUE));
			temp.addAll(_locations.get(TEAM_RED));
			return temp;
		}

		public void addLocationToTeam(int team, Location loc)
		{
			_lastId++;
			if(loc.getId() == 0)
			{
				loc.setId(_lastId);
			}
			_locations.get(team).add(loc);
		}

		public List<Location> getLocationsForTeam(int team)
		{
			return _locations.get(team);
		}

		public Location getRandomLoc(int teamId)
		{
			Location cord = null;
			if(_locations.containsKey(teamId))
			{
				cord = _locations.get(teamId).get(Rnd.get(_locations.get(teamId).size()));
			}
			return cord;
		}

		public void removeLocation(int teamId, int locId)
		{
			for(Location loc : _locations.get(teamId))
			{
				if(loc.getId() == locId)
				{
					_locations.get(teamId).remove(loc);
					break;
				}
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final TvTLocationManager _instance = new TvTLocationManager();
	}
}