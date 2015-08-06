package dwo.gameserver.engine.guardengine;

import dwo.config.Config;
import dwo.config.network.ConfigGuardEngine;
import dwo.gameserver.datatables.sql.queries.GuardEngine;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.guardengine.model.HackType;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.LoginFail;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.07.13
 * Time: 17:42
 */

public class GuardHwidManager
{
	// [HWID, время окончания бана]
	private static final Map<String, Long> _bannedHWIDs = new ConcurrentHashMap<>();

	// [HWID, количество окон]
	private static final Map<String, Integer> _authorizedHWIDs = new ConcurrentHashMap<>();

	private static final Logger Log = LogManager.getLogger("guardengine");

	private static final GuardHwidManager _instance = new GuardHwidManager();

	private GuardHwidManager()
	{
		loadBanList();
	}

	public static GuardHwidManager getInstance()
	{
		return _instance;
	}

	/**
	 * @param hacktype тип нарушения
	 * @return время окончания бана в милиссекундах
	 */
	private static long getTimeToBan(HackType hacktype)
	{
		int days = 0;
		switch(hacktype)
		{
			case PACKET_HACKER:
				days = Config.LAMEGUARD_BANTIME_PACKETHACK;
				break;
			case CLIENT_HACKER:
				days = Config.LAMEGUARD_BANTIME_CLIENT_HACK;
				break;
			case INGAME_BOT:
				days = Config.LAMEGUARD_BANTIME_INGAME_BOT;
				break;
			case BAD_APPLICATION:
				days = Config.LAMEGUARD_BANTIME_BAD_APPLICATION;
				break;
			case PERMANENT:
				return Long.MAX_VALUE;
		}
		return System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
	}

	private void loadBanList()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GuardEngine.LOAD_BAN);
			rset = statement.executeQuery();

			while(rset.next())
			{
				_bannedHWIDs.put(rset.getString("hwid"), Long.parseLong(rset.getString("banEnd")));
			}
		}
		catch(Exception e)
		{
			Log.log(Level.ERROR, "Error while loading BAN's from SQL!", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		Log.log(Level.INFO, "Loaded " + _bannedHWIDs.size() + " hwid(s) ban(s)");
	}

	/***
	 * Добавление "авторизованного" окна в игровой мир
	 * @param hwid HWID клиента
	 * @return {@code true} если добавление прошло успешно
	 */
	public boolean addAuthorizedClient(L2GameClient client, String hwid)
	{
		if(ConfigGuardEngine.GUARD_ENGINE_ENABLE)
		{
			// Проверяем на максимум допустимых окон
			if(ConfigGuardEngine.GUARD_ENGINE_MAX_WINDOWS_RESTRICT_ENABLE)
			{
				if(_authorizedHWIDs.containsKey(hwid))
				{
					if(getActiveWindowsCount(hwid) + 1 > ConfigGuardEngine.GUARD_ENGINE_MAX_WINDOWS_COUNT)
					{
						client.getActiveChar().sendMessage("Превышен максимум допустимых окон.");
						client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
						return false;
					}
				}
			}

			// Проверяем на бан HWID
			if(_bannedHWIDs.containsKey(hwid))
			{
				long currentTime = System.currentTimeMillis();
				long banEnd = _bannedHWIDs.get(hwid);
				if(currentTime > banEnd)
				{
					removeBan(hwid);
					// Добавляем первое окно после бана
					_authorizedHWIDs.put(hwid, getActiveWindowsCount(hwid) + 1);
					return true;
				}
				else
				{
					client.getActiveChar().sendMessage("Для данного ПК вход в игру запрещен.");
					client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
					return false;
				}
			}

			// Добавляем в список авторизованных пользователей окно активного клиента
			_authorizedHWIDs.put(hwid, getActiveWindowsCount(hwid) + 1);
			return true;
		}
		return true;
	}

	/***
	 * Удаление окна из списка активных
	 * @param hwid HWID клиента
	 */
	public void removeAuthorizedClient(String hwid)
	{
		if(_authorizedHWIDs.containsKey(hwid))
		{
			int currentWindowCount = _authorizedHWIDs.get(hwid);
			if(currentWindowCount == 1)
			{
				_authorizedHWIDs.remove(hwid);
			}
			else
			{
				_authorizedHWIDs.put(hwid, currentWindowCount - 1);
			}
		}
	}

	/***
	 * @param hwid HWID клиента
	 * @return количество активных окон для указанного HWID
	 */
	public int getActiveWindowsCount(String hwid)
	{
		return _authorizedHWIDs.containsKey(hwid) ? _authorizedHWIDs.get(hwid) : 0;
	}

	/**
	 * TODO: переделать
	 * @param hwid HWID клиента
	 * @return всех персонажей в мире с указанным HWID
	 */
	public List<L2PcInstance> getTwinks(String hwid)
	{
		List<L2PcInstance> temp = new ArrayList<>();
		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			if(player.getClient().getHWID() != null && player.getClient().getHWID().equals(hwid))
			{
				temp.add(player);
			}
		}
		return temp;
	}

	/**
	 * Добавление бана для указанного HWID
	 * @param hwid HWID клиента
	 * @param account логин клиента
	 * @param ip IP адресс клиента
	 */
	public void tryToBanHWID(String hwid, String ip, String account, HackType hackType, String comment)
	{
		long banEnd = getTimeToBan(hackType);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GuardEngine.INSERT_BAN);
			statement.setString(1, hwid);
			statement.setString(2, account);
			statement.setString(3, ip);
			statement.setString(4, hackType.toString());
			statement.setString(5, comment);
			statement.setLong(6, System.currentTimeMillis());
			statement.setLong(7, banEnd);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			Log.log(Level.ERROR, "Error while writing BAN to SQL!", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_bannedHWIDs.put(hwid, banEnd);
		}
	}

	/**
	 * Удаление записи о бане из SQL
	 * @param hwid HWID клиента, для которого снимаем бан
	 */
	protected void removeBan(String hwid)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GuardEngine.DELETE_BAN);
			statement.setString(1, hwid);
			statement.execute();
		}
		catch(Exception e)
		{
			Log.log(Level.ERROR, "Error while deleting BAN from SQL!", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_bannedHWIDs.remove(hwid);
			Log.log(Level.INFO, "HWID: " + hwid + " removed from BanList.");
		}
	}
}