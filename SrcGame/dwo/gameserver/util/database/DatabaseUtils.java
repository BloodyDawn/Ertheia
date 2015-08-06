package dwo.gameserver.util.database;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.FiltredStatementInterface;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtils
{
	private static Logger _log = LogManager.getLogger(DatabaseUtils.class);

	/**
	 * Закрыть коннект
	 *
	 * @param conn - коннект к базе данных
	 */
	public static void closeConnection(ThreadConnection conn)
	{
		if(conn != null)
		{
			conn.close();
		}
	}

	/**
	 * Закрыть Statement
	 *
	 * @param stmt - Statement
	 */
	public static void closeStatement(FiltredStatementInterface stmt)
	{
		if(stmt != null)
		{
			stmt.close();
		}
	}

	/**
	 * Закрыть ResultSet
	 *
	 * @param rs - ResultSet
	 */
	public static void closeResultSet(ResultSet rs)
	{
		if(rs != null)
		{
			try
			{
				rs.close();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "Error in closing ResultSet '" + rs + "':", e);
			}
		}
	}

	/**
	 * Закрыть коннект, Statement и ResultSet
	 *
	 * @param conn - Connection
	 * @param stmt - Statement
	 * @param rs   - ResultSet
	 */
	public static void closeDatabaseCSR(ThreadConnection conn, FiltredStatementInterface stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
		closeConnection(conn);
	}

	/**
	 * закрыть коннект, Statement
	 *
	 * @param conn - Connection
	 * @param stmt - Statement
	 */
	public static void closeDatabaseCS(ThreadConnection conn, FiltredStatementInterface stmt)
	{
		closeStatement(stmt);
		closeConnection(conn);
	}

	/**
	 * закрыть Statement и ResultSet
	 *
	 * @param stmt - Statement
	 * @param rs   - ResultSet
	 */
	public static void closeDatabaseSR(FiltredStatementInterface stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
	}

	/**
	 * Служит для быстрого формирования
	 * стандартного execute в базу
	 * @param statement запрос в базу
	 */
	public static void executeStatementQuick(String statement)
	{
		if(statement == null || statement.isEmpty())
		{
			return;
		}
		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement(statement);
			offline.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		finally
		{
			closeDatabaseCS(con, offline);
		}
	}

	public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
	{
		String query = "";

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		Integer[][] res = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();

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
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error in query '" + query + "':", e);
		}
		finally
		{
			closeDatabaseCSR(con, statement, rset);
		}
		return res;
	}
}