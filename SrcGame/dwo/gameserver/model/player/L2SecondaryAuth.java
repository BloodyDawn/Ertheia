package dwo.gameserver.model.player;

import dwo.config.Config;
import dwo.gameserver.LoginServerThread;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.lobby.Ex2NDPasswordAck;
import dwo.gameserver.network.game.serverpackets.packet.lobby.Ex2NDPasswordCheck;
import dwo.gameserver.network.game.serverpackets.packet.lobby.Ex2NDPasswordVerify;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.crypt.Base64;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;

public class L2SecondaryAuth
{
	private static final String SELECT_PASSWORD = "SELECT password,wrong_password FROM character_account WHERE account_name=?";
	private static final String INSERT_PASSWORD = "INSERT INTO character_account VALUES (?, ?, ?)";
	private static final String UPDATE_PASSWORD = "UPDATE character_account SET password=? WHERE account_name=?";
	private static final String INSERT_ATTEMPT = "UPDATE character_account SET wrong_password=? WHERE account_name=?";
	private final Logger _log = LogManager.getLogger(L2SecondaryAuth.class);
	private final L2GameClient _activeClient;
	private String _password;
	private int _wrongAttempts;
	private boolean _authed;

	/**
	 * @param activeClient клиент игрока
	 */
	public L2SecondaryAuth(L2GameClient activeClient)
	{
		_activeClient = activeClient;
		_password = null;
		_wrongAttempts = 0;
		_authed = false;
		loadPassword();
	}

	/***
	 * Загрузка из базы вторичного пароля для аккаунта
	 */
	private void loadPassword()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_PASSWORD);
			statement.setString(1, _activeClient.getAccountName());
			rs = statement.executeQuery();
			while(rs.next())
			{
				_password = rs.getString("password");
				_wrongAttempts = rs.getInt("wrong_password");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, L2SecondaryAuth.class.getSimpleName() + ": Error while reading password.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/***
	 * @param password введенный пароль
	 * @return {@code true} если пароль проходит все проверки и удачно сохранился в базу данных
	 */
	public boolean savePassword(String password)
	{
		if(isPasswordExist())
		{
			_log.log(Level.WARN, L2SecondaryAuth.class.getSimpleName() + ": " + _activeClient.getAccountName() + " forced savePassword");
			_activeClient.closeNow();
			return false;
		}

		if(!isValidPassword(password))
		{
			_activeClient.sendPacket(new Ex2NDPasswordAck(Ex2NDPasswordAck.WRONG_PATTERN));
			return false;
		}

		password = cryptPassword(password);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_PASSWORD);
			statement.setString(1, _activeClient.getAccountName());
			statement.setString(2, password);
			statement.setInt(3, 0);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, L2SecondaryAuth.class.getSimpleName() + ": Error while writing password.", e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		_password = password;
		return true;
	}

	/***
	 * Обновление количества неудачных попыток входа в базе
	 * @param attempts количество неудачных попыток ввода
	 * @return {@code true} если количество неудачных попыток ввода было сохранено в базу данных
	 */
	public boolean updateWrongAttempt(int attempts)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_ATTEMPT);
			statement.setInt(1, attempts);
			statement.setString(2, _activeClient.getAccountName());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, L2SecondaryAuth.class.getSimpleName() + ": Error while writing wrong attempts.", e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		return true;
	}

	/***
	 * Смена пароля вторичной авторизации
	 * @param oldPassword старый пароль
	 * @param newPassword новый пароль
	 * @return {@code true} если пароль был успешно сменен
	 */
	public boolean changePassword(String oldPassword, String newPassword)
	{
		if(!isPasswordExist())
		{
			_log.log(Level.WARN, L2SecondaryAuth.class.getSimpleName() + ": " + _activeClient.getAccountName() + " forced changePassword");
			_activeClient.closeNow();
			return false;
		}

		if(!checkPassword(oldPassword, true))
		{
			return false;
		}

		if(!isValidPassword(newPassword))
		{
			_activeClient.sendPacket(new Ex2NDPasswordAck(Ex2NDPasswordAck.WRONG_PATTERN));
			return false;
		}

		newPassword = cryptPassword(newPassword);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_PASSWORD);
			statement.setString(1, newPassword);
			statement.setString(2, _activeClient.getAccountName());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, L2SecondaryAuth.class.getSimpleName() + ": Error while reading password.", e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		_password = newPassword;
		_authed = false;
		return true;
	}

	/***
	 * Проверка введенного пароля
	 * @param password введеный игроком пароль
	 * @param skipAuth пропускать верификацию?
	 * @return {@code true} если пароль валидный
	 */
	public boolean checkPassword(String password, boolean skipAuth)
	{
		password = cryptPassword(password);

		if(!password.equals(_password))
		{
			_wrongAttempts++;
			if(_wrongAttempts < Config.SECOND_AUTH_MAX_ATTEMPTS)
			{
				_activeClient.sendPacket(new Ex2NDPasswordVerify(Ex2NDPasswordVerify.PASSWORD_WRONG, _wrongAttempts));
				updateWrongAttempt(_wrongAttempts);
				return false;
			}
			else
			{
				LoginServerThread.getInstance().sendTempBan(_activeClient.getAccountName(), _activeClient.getConnectionAddress().getHostAddress(), Config.SECOND_AUTH_BAN_TIME);
				//TODO: LoginServerThread.getInstance().sendMail(_activeClient.getAccountName(), "SATempBan", _activeClient.getConnectionAddress().getHostAddress(), Integer.toString(Config.SECOND_AUTH_MAX_ATTEMPTS),Long.toString(Config.SECOND_AUTH_BAN_TIME), Config.SECOND_AUTH_REC_LINK);
				_log.log(Level.WARN, L2SecondaryAuth.class.getSimpleName() + ": " + _activeClient.getAccountName() + " - (" + _activeClient.getConnectionAddress().getHostAddress() + ") has inputted the wrong password " + _wrongAttempts + " times in row.");
				updateWrongAttempt(0);
				_activeClient.close(new Ex2NDPasswordVerify(Ex2NDPasswordVerify.PASSWORD_BAN, Config.SECOND_AUTH_MAX_ATTEMPTS));
				return false;
			}
		}
		if(!skipAuth)
		{
			_authed = true;
			_activeClient.sendPacket(new Ex2NDPasswordVerify(Ex2NDPasswordVerify.PASSWORD_OK, _wrongAttempts));
		}
		updateWrongAttempt(0);
		return true;
	}

	/***
	 * Показываем одно из диалоговых окон вторичной авторизации в зависимости установлен уже пароль на персонажа или нет
	 */
	public void openDialog()
	{
		if(isPasswordExist())
		{
			_activeClient.sendPacket(new Ex2NDPasswordCheck(Ex2NDPasswordCheck.PASSWORD_PROMPT));
		}
		else
		{
			_activeClient.sendPacket(new Ex2NDPasswordCheck(Ex2NDPasswordCheck.PASSWORD_NEW));
		}
	}

	/***
	 * Шиафрация пароля
	 * @param password исходный пароль
	 * @return SHA-хеш указанного пароля
	 */
	private String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			return Base64.encodeBytes(hash);
		}
		catch(NoSuchAlgorithmException e)
		{
			_log.log(Level.ERROR, L2SecondaryAuth.class.getSimpleName() + ": Unsupported Algorithm");
		}
		catch(UnsupportedEncodingException e)
		{
			_log.log(Level.ERROR, L2SecondaryAuth.class.getSimpleName() + ": Unsupported Encoding");
		}
		return null;
	}

	/**
	 * @return {@code true} если пароль на аккаунт уже установлен
	 */
	public boolean isPasswordExist()
	{
		return _password != null;
	}

	/***
	 * @return {@code true} если игрок уже успешно авторизовался
	 */
	public boolean isAuthed()
	{
		return _authed;
	}

	/***
	 * Валидация пароля
	 * @param password пароль
	 * @return {@code true} если пароль подходит по требованиям
	 */
	private boolean isValidPassword(String password)
	{
		if(!Util.isDigit(password))
		{
			return false;
		}

		if(password.length() < 6 || password.length() > 8)
		{
			return false;
		}

		/*for (int i = 0; i < (password.length() - 1); i++)
		{
			char curCh = password.charAt(i);
			char nxtCh = password.charAt(i + 1);

			if ((curCh + 1) == nxtCh)
			{
				return false;
			}
			else if ((curCh - 1) == nxtCh)
			{
				return false;
			}
			else if (curCh == nxtCh)
			{
				return false;
			}
		}

		for (int i = 0; i < (password.length() - 2); i++)
		{
			String toChk = password.substring(i + 1);
			StringBuffer chkEr = new StringBuffer(password.substring(i, i + 2));

			if (toChk.contains(chkEr))
			{
				return false;
			}
			else if (toChk.contains(chkEr.reverse()))
			{
				return false;
			}
		}*/
		_wrongAttempts = 0;
		return true;
	}
}