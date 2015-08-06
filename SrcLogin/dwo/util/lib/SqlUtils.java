package dwo.util.lib;

import dwo.database.DatabaseUtils;
import dwo.database.FiltredPreparedStatement;
import dwo.database.L2DatabaseFactory;
import dwo.database.ThreadConnection;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

public class SqlUtils
{
	private static Logger _log = LogManager.getLogger(SqlUtils.class);

	private SqlUtils()
	{
	}

	// =========================================================
	// Property - Public
	public static SqlUtils getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================
	// Method - Public
	public static Integer getIntValue(String resultField, String tableName, String whereClause)
	{
		String query = "";
		Integer res = null;

		ThreadConnection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[]{
				resultField
			}, tableName, whereClause, true);

			FiltredPreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery();

			if(rset.next())
			{
				res = rset.getInt(1);
			}

			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error in query '" + query + "':", e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}

		return res;
	}

	public static Integer[] getIntArray(String resultField, String tableName, String whereClause)
	{
		String query = "";
		Integer[] res = null;

		ThreadConnection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[]{
				resultField
			}, tableName, whereClause, false);
			FiltredPreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery();

			int rows = 0;

			while(rset.next())
			{
				rows++;
			}

			if(rows == 0)
			{
				return new Integer[0];
			}

			res = new Integer[rows - 1];

			rset.first();

			int row = 0;
			while(rset.next())
			{
				res[row] = rset.getInt(1);
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "mSGI: Error in query '" + query + "':", e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}

		return res;
	}

	public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
	{
		long start = System.currentTimeMillis();

		String query = "";

		ThreadConnection con = null;

		Integer[][] res = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
			FiltredPreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery();

			int rows = 0;
			while(rset.next())
			{
				rows++;
			}

			res = new Integer[rows - 1][resultFields.length];

			rset.first();

			int row = 0;
			while(rset.next())
			{
				for(int i = 0; i < resultFields.length; i++)
				{
					res[row][i] = rset.getInt(i + 1);
				}
				row++;
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error in query '" + query + "':", e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}

		_log.log(Level.WARN, "Get all rows in query '" + query + "' in " + (System.currentTimeMillis() - start) + "ms");
		return res;
	}

	private static class SingletonHolder
	{
		protected static final SqlUtils _instance = new SqlUtils();
	}
}
