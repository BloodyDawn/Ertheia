package dwo.gameserver.engine.databaseengine;

import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FiltredPreparedStatement implements FiltredStatementInterface
{
	private final PreparedStatement myStatement;

	public FiltredPreparedStatement(PreparedStatement statement)
	{
		myStatement = statement;
	}

	public ResultSet executeQuery() throws SQLException
	{
		return myStatement.executeQuery();
	}

	@Override
	public void close()
	{
		try
		{
			myStatement.close();
		}
		catch(SQLException e)
		{
			// Ignored
		}
	}

	public boolean execute() throws SQLException
	{
		return myStatement.execute();
	}

	public ResultSet executeQuery(String sql) throws SQLException
	{
		return myStatement.executeQuery(sql);
	}

	public void setInt(int index, int val) throws SQLException
	{
		myStatement.setInt(index, val);
	}

	public void setString(int index, String val) throws SQLException
	{
		myStatement.setString(index, val);
	}

	public void setLong(int index, long val) throws SQLException
	{
		myStatement.setLong(index, val);
	}

	public void setNull(int index, int val) throws SQLException
	{
		myStatement.setNull(index, val);
	}

	public void setDouble(int index, double val) throws SQLException
	{
		myStatement.setDouble(index, val);
	}

	public void setBytes(int index, byte[] data) throws SQLException
	{
		myStatement.setBytes(index, data);
	}

	public int executeUpdate() throws SQLException
	{
		return myStatement.executeUpdate();
	}

	public void setBoolean(int index, boolean val) throws SQLException
	{
		myStatement.setBoolean(index, val);
	}

	public void setEscapeProcessing(boolean val) throws SQLException
	{
		myStatement.setEscapeProcessing(val);
	}

	public void setByte(int index, byte val) throws SQLException
	{
		myStatement.setByte(index, val);
	}

	public void setDate(int index, Date val) throws SQLException
	{
		myStatement.setDate(index, val);
	}

	public void setVars(Object... vars) throws SQLException
	{
		Number n;
		long long_val;
		double double_val;
		for(int i = 0; i < vars.length; i++)
		{
			if(vars[i] instanceof Number)
			{
				n = (Number) vars[i];
				long_val = n.longValue();
				double_val = n.doubleValue();
				if(long_val == double_val)
				{
					setLong(i + 1, long_val);
				}
				else
				{
					setDouble(i + 1, double_val);
				}
			}
			else if(vars[i] instanceof String)
			{
				setString(i + 1, (String) vars[i]);
			}
		}
	}

	public void clearParameters() throws SQLException
	{
		myStatement.clearParameters();
	}

	public int getUpdateCount() throws SQLException
	{
		return myStatement.getUpdateCount();
	}

	public ResultSet getResultSet() throws SQLException
	{
		return myStatement.getResultSet();
	}

	public ParameterMetaData getParameterMetaData() throws SQLException
	{
		return myStatement.getParameterMetaData();
	}
}