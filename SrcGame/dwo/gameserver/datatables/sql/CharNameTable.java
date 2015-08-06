package dwo.gameserver.datatables.sql;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.queries.Characters;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CharNameTable
{
	private static Logger _log = LogManager.getLogger(CharNameTable.class);

	private final Map<Integer, String> _chars;
	private final TIntIntHashMap _accessLevels;

	private CharNameTable()
	{
		_chars = new FastMap<>();
		_accessLevels = new TIntIntHashMap();
		if(Config.CACHE_CHAR_NAMES)
		{
			loadAll();
		}
	}

	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addName(L2PcInstance player)
	{
		if(player != null)
		{
			addName(player.getObjectId(), player.getName());
			_accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
		}
	}

	public void addName(int objectId, String name)
	{
		if(name != null)
		{
			if(!name.equals(_chars.get(objectId)))
			{
				_chars.put(objectId, name);
			}
		}
	}

	public void removeName(int objId)
	{
		_chars.remove(objId);
		_accessLevels.remove(objId);
	}

	public int getIdByName(String name)
	{
		if(name == null || name.isEmpty())
		{
			return -1;
		}

		Iterator<Entry<Integer, String>> it = _chars.entrySet().iterator();

		Entry<Integer, String> pair;
		while(it.hasNext())
		{
			pair = it.next();
			if(pair.getValue().equalsIgnoreCase(name))
			{
				return pair.getKey();
			}
		}

		if(Config.CACHE_CHAR_NAMES)
		{
			return -1;
		}

		int id = -1;
		int accessLevel = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_ACCESSLEVEL_BY_NAME);
			statement.setString(1, name);
			rset = statement.executeQuery();
			while(rset.next())
			{
				id = rset.getInt(1);
				accessLevel = rset.getInt(2);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not check existing char name: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		if(id > 0)
		{
			_chars.put(id, name);
			_accessLevels.put(id, accessLevel);
			return id;
		}

		return -1; // not found
	}

	public String getNameById(int id)
	{
		if(id <= 0)
		{
			return null;
		}

		String name = _chars.get(id);
		if(name != null)
		{
			return name;
		}

		if(Config.CACHE_CHAR_NAMES)
		{
			return null;
		}

		int accessLevel = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_ACCESSLEVEL_BY_CHARID);
			statement.setInt(1, id);
			rset = statement.executeQuery();
			while(rset.next())
			{
				name = rset.getString(1);
				accessLevel = rset.getInt(2);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not check existing char id: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		if(name != null && !name.isEmpty())
		{
			_chars.put(id, name);
			_accessLevels.put(id, accessLevel);
			return name;
		}

		return null; //not found
	}

	public int getAccessLevelById(int objectId)
	{
		return getNameById(objectId) != null ? _accessLevels.get(objectId) : 0;
	}

	public boolean doesCharNameExist(String name)
	{
		synchronized(this)
		{
			boolean result = true;

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Characters.SELECT_ACCOUNT_NAME_BY_CHAR_NAME);
				statement.setString(1, name);
				rset = statement.executeQuery();
				result = rset.next();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "Could not check existing charname: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			}
			return result;
		}
	}

	public int accountCharNumber(String account)
	{
		int number = 0;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_COUNT_CHAR);
			statement.setString(1, account);
			rset = statement.executeQuery();
			while(rset.next())
			{
				number = rset.getInt(1);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not check existing char number: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		return number;
	}

	private void loadAll()
	{
		String name;
		int id = -1;
		int accessLevel = 0;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_ACCESSLEVEL_ALL);
			rset = statement.executeQuery();
			while(rset.next())
			{
				id = rset.getInt(1);
				name = rset.getString(2);
				accessLevel = rset.getInt(3);
				_chars.put(id, name);
				_accessLevels.put(id, accessLevel);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not load char name: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _chars.size() + " char names.");
	}

	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}
}
