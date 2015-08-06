package dwo.xmlrpcserver.XMLServices;

import dwo.dao.queries.Account;
import dwo.database.FiltredPreparedStatement;
import dwo.database.L2DatabaseFactory;
import dwo.database.ThreadConnection;
import dwo.util.crypt.PasswordHash;
import dwo.xmlrpcserver.model.Message;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Account management service.
 *
 * @author Yorie
 */
public class PlayerService extends Base
{
	/**
	 * Checks if user typed correct information for existing account.
	 *
	 * @param name Account name.
	 * @param password Account password.
	 * @return
	 */
	public String login(String name, String password)
	{
		Logger log = LogManager.getLogger(PlayerService.class);

		String hash;
		try
		{
			hash = PasswordHash.encrypt(password);
		}
		catch(Exception e)
		{
			return json(new Message(Message.MessageType.FAILED, "Неверный пароль."));
		}

		ThreadConnection con;
		FiltredPreparedStatement statement;
		ResultSet rset;

		boolean ok = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM `accounts` WHERE `login` = ? AND `password` = ?");
			statement.setString(1, name);
			statement.setString(2, hash);
			rset = statement.executeQuery();

			if(rset.next())
			{
				ok = true;
			}

			rset.close();
			statement.close();
		}
		catch(SQLException e)
		{
			return json(new Message(Message.MessageType.FAILED));
		}

		return ok ? json(new Message(Message.MessageType.OK)) : json(new Message(Message.MessageType.FAILED));
	}

	/**
	 * Checks if account with specified name already exists.
	 *
	 * @param account Account name.
	 * @return
	 */
	public String exists(String account)
	{
		boolean exists = false;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(Account.ACCOUNT_EXISTS);
			statement.setString(1, account);
			resultSet = statement.executeQuery();

			if(resultSet.next() && resultSet.getInt(1) > 0)
			{
				exists = true;
			}
		}
		catch(SQLException e)
		{
			exists = true;
		}
		finally
		{
			databaseClose(true);
		}

		return json(new Message(Message.MessageType.OK, "", json(exists)));
	}

	/**
	 * Registers account.
	 *
	 * @param account Account name.
	 * @param password Account password.
	 * @return
	 */
	public String register(String account, String password, String ip)
	{
		boolean ok = true;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();

			// Check if account with same name is exists
			statement = conn.prepareStatement(Account.ACCOUNT_EXISTS);
			statement.setString(1, account);
			resultSet = statement.executeQuery();
			if(resultSet.next() && resultSet.getInt(1) > 0)
			{
				return json(new Message(Message.MessageType.ERROR, "Аккаунт с таким именем уже существует."));
			}

			statement = conn.prepareStatement(Account.ADD_ACCOUNT);
			statement.setString(1, account);
			statement.setString(2, PasswordHash.encrypt(password));
			statement.setLong(3, System.currentTimeMillis());
			statement.setInt(4, 0);
			statement.setString(5, ip);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			log.error("Cannot register account via RPC.", e);
			ok = false;
		}
		finally
		{
			databaseClose(false);
		}

		return ok ? json(new Message(Message.MessageType.OK)) : json(new Message(Message.MessageType.FAILED, "Произошла ошибка при работе с базой данных"));
	}

	/**
	 * Смена пароля от аккаунта.
	 *
	 * @param account Имя аккаунта.
	 * @param password Новый пароль.
	 * @return
	 */
	public String changePassword(String account, String password)
	{
		boolean ok = true;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(Account.CHANGE_PASSWORD);
			statement.setString(1, PasswordHash.encrypt(password));
			statement.setString(2, account.toLowerCase());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			log.error("Cannot change account password via RPC.", e);
			ok = false;
		}
		finally
		{
			databaseClose(false);
		}

		return ok ? json(new Message(Message.MessageType.OK)) : json(new Message(Message.MessageType.FAILED, "Произошла ошибка при работе с базой данных"));
	}
}
