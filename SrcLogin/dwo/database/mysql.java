package dwo.database;

import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class mysql
{
	private static Logger _log = LogManager.getLogger(mysql.class);

	public static boolean setEx(L2DatabaseFactory db, String query, Object... vars)
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		FiltredPreparedStatement pstatement = null;
		try
		{
			if(db == null)
			{
				db = L2DatabaseFactory.getInstance();
			}
			con = db.getConnection();
			if(vars.length == 0)
			{
				statement = con.createStatement();
				statement.executeUpdate(query);
			}
			else
			{
				pstatement = con.prepareStatement(query);
				pstatement.setVars(vars);
				pstatement.executeUpdate();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not execute update '" + query + "': " + e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, vars.length == 0 ? statement : pstatement);
		}
		return true;
	}

	public static boolean set(String query, Object... vars)
	{
		return setEx(null, query, vars);
	}

	public static boolean set(String query)
	{
		return setEx(null, query);
	}

	public static Object get(String query)
	{
		Object ret = null;
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery(query + " LIMIT 1");
			ResultSetMetaData md = rset.getMetaData();

			if(rset.next())
			{
				if(md.getColumnCount() > 1)
				{
					Map<String, Object> tmp = new ConcurrentHashMap<>();
					for(int i = md.getColumnCount(); i > 0; i--)
					{
						tmp.put(md.getColumnName(i), rset.getObject(i));
					}
					ret = tmp;
				}
				else
				{
					ret = rset.getObject(1);
				}
			}

		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not execute query '" + query + "': " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return ret;
	}

	public static FastList<HashMap<String, Object>> getAll(String query)
	{
		FastList<HashMap<String, Object>> ret = new FastList<>();
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery(query);
			ResultSetMetaData md = rset.getMetaData();

			while(rset.next())
			{
				HashMap<String, Object> tmp = new HashMap<>();
				for(int i = md.getColumnCount(); i > 0; i--)
				{
					tmp.put(md.getColumnName(i), rset.getObject(i));
				}
				ret.add(tmp);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not execute query '" + query + "': " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return ret;
	}

	public static FastList<Object> get_array(L2DatabaseFactory db, String query)
	{
		FastList<Object> ret = new FastList<>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			if(db == null)
			{
				db = L2DatabaseFactory.getInstance();
			}
			con = db.getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			ResultSetMetaData md = rset.getMetaData();

			while(rset.next())
			{
				if(md.getColumnCount() > 1)
				{
					Map<String, Object> tmp = new ConcurrentHashMap<>();
					for(int i = 0; i < md.getColumnCount(); i++)
					{
						tmp.put(md.getColumnName(i + 1), rset.getObject(i + 1));
					}
					ret.add(tmp);
				}
				else
				{
					ret.add(rset.getObject(1));
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not execute query '" + query + "': " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return ret;
	}

	public static FastList<Object> get_array(String query)
	{
		return get_array(null, query);
	}

	public static int simple_get_int(String ret_field, String table, String where)
	{
		String query = "SELECT " + ret_field + " FROM `" + table + "` WHERE " + where + " LIMIT 1;";

		int res = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();

			if(rset.next())
			{
				res = rset.getInt(1);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "mSGI: Error in query '" + query + "':" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return res;
	}

	public static Integer[][] simple_get_int_array(L2DatabaseFactory db, String[] ret_fields, String table, String where)
	{
		long start = System.currentTimeMillis();

		String fields = null;
		for(String field : ret_fields)
		{
			if(fields != null)
			{
				fields += ",";
				fields += '`' + field + '`';
			}
			else
			{
				fields = '`' + field + '`';
			}
		}

		String query = "SELECT " + fields + " FROM `" + table + "` WHERE " + where;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		Integer[][] res = null;

		try
		{
			if(db == null)
			{
				db = L2DatabaseFactory.getInstance();
			}
			con = db.getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();

			ArrayList<Integer[]> al = new ArrayList<>();
			int row = 0;
			while(rset.next())
			{
				Integer[] tmp = new Integer[ret_fields.length];
				for(int i = 0; i < ret_fields.length; i++)
				{
					tmp[i] = rset.getInt(i + 1);
				}
				al.add(row, tmp);
				row++;
			}

			res = al.toArray(new Integer[row][ret_fields.length]);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "mSGIA: Error in query '" + query + "':" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.log(Level.WARN, "Get all rows in query '" + query + "' in " + (System.currentTimeMillis() - start) + "ms");
		return res;
	}

	public static Integer[][] simple_get_int_array(String[] ret_fields, String table, String where)
	{
		return simple_get_int_array(null, ret_fields, table, where);
	}
}