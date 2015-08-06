package dwo.gameserver.instancemanager;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Map;

public class GlobalVariablesManager
{
	private static final Logger _log = LogManager.getLogger(GlobalVariablesManager.class);

	private static final String LOAD_VAR = "SELECT var,value FROM global_variables";
	private static final String SAVE_VAR = "INSERT INTO global_variables (var,value) VALUES (?,?) ON DUPLICATE KEY UPDATE value=?";

	private final Map<String, String> _variablesMap;

	private GlobalVariablesManager()
	{
		_variablesMap = new FastMap<String, String>().shared();

		loadVars();
	}

	public static GlobalVariablesManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void loadVars()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOAD_VAR);
			rset = statement.executeQuery();

			String var;
			String value;
			while(rset.next())
			{
				var = rset.getString(1);
				value = rset.getString(2);
				_variablesMap.put(var, value);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "GlobalVariablesManager: problem while loading variables: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void saveVars()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SAVE_VAR);

			for(Map.Entry<String, String> stringStringEntry : _variablesMap.entrySet())
			{
				statement.setString(1, stringStringEntry.getKey());
				statement.setString(2, stringStringEntry.getValue());
				statement.setString(3, stringStringEntry.getValue());
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "GlobalVariablesManager: problem while saving variables: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void storeVariable(String var, String value)
	{
		_variablesMap.put(var, value);
	}

	public boolean isVariableStored(String var)
	{
		return _variablesMap.containsKey(var);
	}

	public String getStoredVariable(String var)
	{
		return _variablesMap.get(var);
	}

	/**
	 * Удаляет переменную из базы
	 * @param var перменная на удаление
	 */
	public void deleteVariableAndStore(String var)
	{
		// Удаляем переменную из памяти
		_variablesMap.remove(var);

		// Вычищаем таблицу в базе
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM global_variables");
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "GlobalVariablesManager: problem while deleting variables: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);

			// Сохраняем обновленное содержимое массива в памяти
			saveVars();
		}
	}

	private static class SingletonHolder
	{
		protected static final GlobalVariablesManager _instance = new GlobalVariablesManager();
	}
}