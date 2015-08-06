package dwo.gameserver.datatables.sql;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.player.AccountShareData;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public class AccountShareDataTable
{
	private static final Logger _log = LogManager.getLogger(AccountShareDataTable.class);

	private final Map<String, Map<String, AccountShareData>> _accountData;

	public AccountShareDataTable()
	{
		_accountData = new FastMap<String, Map<String, AccountShareData>>().shared();
		load();
	}

	public static AccountShareDataTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTER_SHAREDATA);
			rset = statement.executeQuery();
			int loaded = 0;
			AccountShareData data = null;

			while(rset.next())
			{
				data = addAccountData(rset.getString("account_name"), rset.getString("var"), rset.getString("value"));
				data.skipDbUpdate();

				loaded++;
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + loaded + " data for " + _accountData.size() + " accounts.");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not load character_sharedata: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * @param account имя аккаунта
	 * @param var имя переменной
	 * @return общая переменная для аккаунта игрока
	 */
	public AccountShareData getAccountData(String account, String var)
	{
		if(!_accountData.containsKey(account))
		{
			return null;
		}
		return _accountData.get(account).get(var);
	}

	/**
	 * @param account имя аккаунта
	 * @param var имя переменной
	 * @param value значение переменной
	 * @return новая, общая для персонажей на аккаунте, переменная
	 */
	public AccountShareData addAccountData(String account, String var, String value)
	{
		AccountShareData data = new AccountShareData(account, var, value);
		if(_accountData.containsKey(account))
		{
			_accountData.get(account).put(var, data);
		}
		else
		{
			Map<String, AccountShareData> map = new FastMap<String, AccountShareData>().shared();
			map.put(var, data);
			_accountData.put(account, map);
		}
		return data;
	}

	/**
	 * @param account имя акаунта
	 * @param var переменная
	 * @param defaultValue значение переменной по-умолчанию
	 * @return новый AccountData, если запрашиваемый не был найден
	 */
	public AccountShareData getAccountData(String account, String var, String defaultValue)
	{
		if(_accountData.containsKey(account) && _accountData.get(account).containsKey(var))
		{
			return _accountData.get(account).get(var);
		}
		else
		{
			AccountShareData data = addAccountData(account, var, defaultValue);
			data.updateInDb();
			return data;
		}
	}

	/**
	 * Сохранение всех переменных из памяти в базу
	 */
	public void updateInDb()
	{
		for(Map<String, AccountShareData> account : _accountData.values())
		{
			for(AccountShareData data : account.values())
			{
				data.updateInDb();
			}
		}
	}

	public Collection<Map<String, AccountShareData>> getAccountDatas()
	{
		return _accountData.values();
	}

	private static class SingletonHolder
	{
		protected static final AccountShareDataTable _instance = new AccountShareDataTable();
	}
}
