package dwo.gameserver.model.player;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AccountShareData
{
	private static final Logger _log = LogManager.getLogger(AccountShareData.class);

	private final String _account;
	private String _var;
	private String _value;
	private boolean _isUpdateNeeded = true;

	public AccountShareData(String account_name, String var, String value)
	{
		_account = account_name;
		_var = var;
		_value = value;
	}

	public String getAccountName()
	{
		return _account;
	}

	public String getVar()
	{
		return _var;
	}

	public String getValue()
	{
		return _value;
	}

	public void setValue(String value)
	{
		_value = value;
		_isUpdateNeeded = true;
	}

	public int getIntValue(int def)
	{
		if(Util.isDigit(_value))
		{
			return Integer.parseInt(_value);
		}
		return def;
	}

	public long getLongValue()
	{
		return getLongValue(0);
	}

	public long getLongValue(long def)
	{
		if(Util.isDigit(_value))
		{
			return Long.parseLong(_value);
		}
		return def;
	}

	public int getIntValue()
	{
		return getIntValue(0);
	}

	public void skipDbUpdate()
	{
		_isUpdateNeeded = false;
	}

	public void updateInDb()
	{
		if(!_isUpdateNeeded)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.REPLACE_CHARACTER_SHAREDATA);
			statement.setString(1, _account);
			statement.setString(2, _var);
			statement.setString(3, _value);
			statement.executeUpdate();
			_isUpdateNeeded = false;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store character_sharedata: " + this + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	@Override
	public String toString()
	{
		return '[' + _account + '/' + _var + '=' + _value + "/ " + _isUpdateNeeded + ']';
	}
}
