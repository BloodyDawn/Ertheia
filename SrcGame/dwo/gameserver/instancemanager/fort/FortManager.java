package dwo.gameserver.instancemanager.fort;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

public class FortManager
{
	protected static final Logger _log = LogManager.getLogger(FortManager.class);
	private List<Fort> _forts;

	private FortManager()
	{
	}

	public static FortManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void init()
	{
		_log.log(Level.INFO, getClass().getSimpleName() + ": Initializing...");
		load();
		_log.log(Level.INFO, getClass().getSimpleName() + " Loaded " + getForts().size() + " fortresses.");
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT id FROM fort ORDER BY id");
			rs = statement.executeQuery();

			while(rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Exception in loadFortData(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	public int findNearestFortIndex(L2Object obj)
	{
		return findNearestFortIndex(obj, Long.MAX_VALUE);
	}

	public int findNearestFortIndex(L2Object obj, long maxDistance)
	{
		int index = getFortIndex(obj);
		if(index < 0)
		{
			double distance;
			Fort fort;
			for(int i = 0; i < getForts().size(); i++)
			{
				fort = getForts().get(i);
				if(fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if(maxDistance > distance)
				{
					maxDistance = (long) distance;
					index = i;
				}
			}
		}
		return index;
	}

	public Fort getFortById(int fortId)
	{
		for(Fort f : getForts())
		{
			if(f.getFortId() == fortId)
			{
				return f;
			}
		}
		return null;
	}

	public Fort getFortByOwner(L2Clan clan)
	{
		for(Fort f : getForts())
		{
			if(f.getOwnerClan().equals(clan))
			{
				return f;
			}
		}
		return null;
	}

	public Fort getFort(String name)
	{
		for(Fort f : getForts())
		{
			if(f.getName().equalsIgnoreCase(name.trim()))
			{
				return f;
			}
		}
		return null;
	}

	public Fort getFort(int x, int y, int z)
	{
		for(Fort f : getForts())
		{
			if(f.checkIfInZone(x, y, z))
			{
				return f;
			}
		}
		return null;
	}

	public Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public int getFortIndex(int fortId)
	{
		Fort fort;
		for(int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if(fort != null && fort.getFortId() == fortId)
			{
				return i;
			}
		}
		return -1;
	}

	public int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for(int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if(fort != null && fort.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}

	public List<Fort> getForts()
	{
		if(_forts == null)
		{
			_forts = new FastList<>();
		}
		return _forts;
	}

	public void spawnDoors()
	{
		for(Fort fort : _forts)
		{
			fort.spawnDoors();
		}
	}

	private static class SingletonHolder
	{
		protected static final FortManager _instance = new FortManager();
	}
}