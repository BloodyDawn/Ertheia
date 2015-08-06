package dwo.gameserver.datatables.sql;

import dwo.gameserver.model.world.zone.L2Territory;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class LocationsTable
{
	private static final Map<Integer, L2Territory> _territory = new HashMap<>();
	private static Logger _log = LogManager.getLogger(LocationsTable.class);

	private LocationsTable()
	{
		load();
	}

	public static LocationsTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public void load()
	{
		_territory.clear();

		Integer[][] point = DatabaseUtils.get2DIntArray(new String[]{
			"loc_id", "loc_x", "loc_y", "loc_zmin", "loc_zmax", "proc"
		}, "locations", "loc_id > 0");
		for(Integer[] row : point)
		{
			Integer terr = row[0];
			if(terr == null)
			{
				_log.log(Level.ERROR, "Null territory!");
				continue;
			}

			if(_territory.get(terr) == null)
			{
				L2Territory t = new L2Territory(terr);
				_territory.put(terr, t);
			}
			_territory.get(terr).add(row[1], row[2], row[3], row[4], row[5]);
		}
	}

	public int[] getRandomPoint(int terr)
	{
		return _territory.get(terr).getRandomPoint();
	}

	public int getProcMax(int terr)
	{
		return _territory.get(terr).getProcMax();
	}

	private static class SingletonHolder
	{
		protected static final LocationsTable _instance = new LocationsTable();
	}
}