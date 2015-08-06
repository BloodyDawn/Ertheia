package dwo.gameserver.instancemanager.votemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.08.12
 * Time: 8:37
 */

public class L2TopManager
{
	private static final Logger _log = LogManager.getLogger("vote");

	private static final String SELECT_PLAYER_OBJID = "SELECT charId FROM characters WHERE char_name=?";
	private static final String SELECT_CHARACTER_MMOTOP_DATA = "SELECT * FROM character_vote_l2top WHERE id=? AND date=? AND multipler=?";
	private static final String INSERT_L2TOP_DATA = "INSERT INTO character_vote_l2top (date, id, nick, multipler) values (?,?,?,?)";
	private static final String DELETE_L2TOP_DATA = "DELETE FROM character_vote_l2top WHERE date<?";
	private static final String SELECT_MULTIPLER_L2TOP_DATA = "SELECT multipler FROM character_vote_l2top WHERE id=? AND has_reward=0";
	private static final String UPDATE_L2TOP_DATA = "UPDATE character_vote_l2top SET has_reward=1 WHERE id=?";
	private static L2TopManager _instance;
	BufferedReader readerSms;
	BufferedReader readerWeb;

	public L2TopManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConnectAndUpdate(), Config.L2_TOP_MANAGER_INTERVAL, Config.L2_TOP_MANAGER_INTERVAL);
	}

	public static L2TopManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new L2TopManager();
		}
		return _instance;
	}

	/***
	 * Чтение файлов (SMS/WEB) с хоста L2Top
	 */
	public void readPageFromServer()
	{
		try
		{
			URL url = new URL(Config.L2_TOP_SMS_ADDRESS);
			readerSms = new BufferedReader(new InputStreamReader(url.openStream(), "cp1251"));

			url = new URL(Config.L2_TOP_WEB_ADDRESS);
			readerWeb = new BufferedReader(new InputStreamReader(url.openStream(), "cp1251"));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Server didn't response. ");
		}
	}

	/***
	 * Парсинг полученной с L2Top страницы
	 * @param sms {@code true} если страница является страницей для СМС голосования
	 */
	private void parsePageFromServer(boolean sms)
	{
		try
		{
			BufferedReader in = sms ? readerSms : readerWeb;

			if(in == null)
			{
				return;
			}

			// Пропускаем " Статистика web голосования для сервера http://godworld.ru/ "
			in.readLine();

			String str;
			while((str = in.readLine()) != null)
			{
				try
				{
					Calendar calendar = Calendar.getInstance();
					String[] line = str.split("\t");
					String[] date = line[0].split(" ")[0].split("-");
					String[] time = line[0].split(" ")[1].split(":");

					calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
					calendar.set(Calendar.MONTH, Integer.parseInt(date[1]));
					calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
					calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
					calendar.set(Calendar.SECOND, Integer.parseInt(time[2]));
					String nick = line[1];

					// Если есть префикс проверяем его
					if(!Config.L2_TOP_PREFIX.isEmpty())
					{
						String[] prefix = nick.split("-");
						if(prefix.length == 2 && prefix[0].equals(Config.L2_TOP_PREFIX))
						{
							nick = prefix[1];
						}
						else
						{
							continue;
						}
					}

					if(!isValidNick(nick))
					{
						continue;
					}

					int mul = 1;

					// Если это смс читаем количество голосов.
					if(sms)
					{
						mul = Integer.parseInt(line[2].replace("x", ""));
					}

					long voteTime = calendar.getTimeInMillis() / 1000;

					if(voteTime + Config.L2_TOP_SAVE_DAYS * 86400 > System.currentTimeMillis() / 1000)
					{
						checkAndSave(voteTime, nick, mul);
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while parsePageFromServer() line: " + str + " e:", e);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while parsePageFromServer(). ", e);
		}
	}

	/***
	 * Проверка полученных данных о голосовании и сохранение их в базу данных
	 * @param date дата (Timestamp) голосования
	 * @param nick ник проголосовавшего игрока
	 * @param mult количество голосов
	 */
	private void checkAndSave(long date, String nick, int mult)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement selectObjectStatement = null;
		FiltredPreparedStatement selectL2topStatement = null;
		FiltredPreparedStatement insertStatement = null;
		ResultSet rsetObject = null;
		ResultSet rsetL2top = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			selectObjectStatement = con.prepareStatement(SELECT_PLAYER_OBJID);
			selectObjectStatement.setString(1, nick);
			rsetObject = selectObjectStatement.executeQuery();
			int objId = 0;
			if(rsetObject.next())
			{
				objId = rsetObject.getInt("charId");
			}
			if(objId > 0)
			{
				selectL2topStatement = con.prepareStatement(SELECT_CHARACTER_MMOTOP_DATA);
				selectL2topStatement.setInt(1, objId);
				selectL2topStatement.setLong(2, date);
				selectL2topStatement.setInt(3, mult);
				rsetL2top = selectL2topStatement.executeQuery();
				if(!rsetL2top.next())
				{
					insertStatement = con.prepareStatement(INSERT_L2TOP_DATA);
					insertStatement.setLong(1, date);
					insertStatement.setInt(2, objId);
					insertStatement.setString(3, nick);
					insertStatement.setInt(4, mult);
					insertStatement.execute();
					insertStatement.clearParameters();
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while checkAndSave()", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, selectObjectStatement, rsetObject);
			DatabaseUtils.closeDatabaseSR(selectL2topStatement, rsetL2top);
			DatabaseUtils.closeStatement(insertStatement);
		}
	}

	/***
	 * Очистка неактуальных записей из базы данных о голосованиях
	 */
	private void clean()
	{
		synchronized(this)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -Config.L2_TOP_SAVE_DAYS);
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(DELETE_L2TOP_DATA);
				statement.setLong(1, calendar.getTimeInMillis() / 1000);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while clean()", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
				giveReward();
			}
		}
	}

	/***
	 * Выдача наград голосовавшим, которые в данный момент находятся в игре
	 */
	private void giveReward()
	{
		synchronized(this)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement selectMultStatement = null;
			FiltredPreparedStatement updateStatement = null;
			ResultSet rsetMult = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
				{
					int objId = player.getObjectId();
					int mult = 0;
					selectMultStatement = con.prepareStatement(SELECT_MULTIPLER_L2TOP_DATA);
					selectMultStatement.setInt(1, objId);
					rsetMult = selectMultStatement.executeQuery();
					while(rsetMult.next())
					{
						mult += rsetMult.getInt("multipler");
					}

					updateStatement = con.prepareStatement(UPDATE_L2TOP_DATA);
					updateStatement.setInt(1, objId);
					updateStatement.executeUpdate();
					if(mult > 0)
					{
						if(player.getLang().equals("ru"))
						{
							player.sendMessage("Спасибо за Ваш голос в рейтинге L2Top. C наилучшими пожеланиями, Администрация GodWorld.");
						}
						else
						{
							player.sendMessage("Thank you for your vote in L2Top raiting. Best regards, GodWorld Administration. ");
						}

						if(Config.L2_TOP_REWARD[0] == -100) // PC Bang
						{
							player.setPcBangPoints(player.getPcBangPoints() + Config.L2_TOP_REWARD[1] * mult);
							_log.log(Level.INFO, "L2TOP: " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.L2_TOP_REWARD[0] + "count:" + Config.L2_TOP_REWARD[1] * mult + ']');
						}
						else if(Config.L2_TOP_REWARD[0] == -200) // Clan reputation
						{
							if(player.getClan() != null)
							{
								player.getClan().addReputationScore(Config.L2_TOP_REWARD[1] * mult, true);
								_log.log(Level.INFO, getClass().getSimpleName() + ": " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.L2_TOP_REWARD[0] + "count:" + Config.L2_TOP_REWARD[1] * mult + ']');
							}
							else
							{
								L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.DONATION, Config.L2_TOP_REWARD_NO_CLAN[0], Config.L2_TOP_REWARD_NO_CLAN[1] * mult, null);
								player.addItem(ProcessType.DONATION, item, null, true);
								_log.log(Level.INFO, getClass().getSimpleName() + ": " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.L2_TOP_REWARD_NO_CLAN[0] + "count:" + Config.L2_TOP_REWARD_NO_CLAN[1] * mult + ']');
							}
						}
						else if(Config.MMO_TOP_REWARD[0] == -300) // Fame
						{
							player.setFame(player.getFame() + Config.L2_TOP_REWARD[1] * mult);
							_log.log(Level.INFO, getClass().getSimpleName() + ": " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.L2_TOP_REWARD[0] + "count:" + Config.L2_TOP_REWARD[1] * mult + ']');
						}
						else
						{
							L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.DONATION, Config.L2_TOP_REWARD[0], Config.L2_TOP_REWARD[1] * mult, null);
							player.addItem(ProcessType.DONATION, item, null, true);
							_log.log(Level.INFO, getClass().getSimpleName() + ": " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.L2_TOP_REWARD[0] + "count:" + Config.L2_TOP_REWARD[1] * mult + ']');
						}
					}
				}
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while giveReward()", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, selectMultStatement, rsetMult);
				DatabaseUtils.closeDatabaseCS(con, updateStatement);
			}
		}
	}

	/***
	 * @param nick имя проголосовавшего игрока
	 * @return {@code true} если указанный ник является валидным
	 */
	private boolean isValidNick(String nick)
	{
		if(!CharNameTable.getInstance().doesCharNameExist(nick))
		{
			return false;
		}
		if(nick.length() < 1 || nick.length() > 16)
		{
			return false;
		}
		return !(!Util.isAlphaNumeric(nick) || !Util.isValidName(nick));
	}

	/***
	 * Основной таск на обновление данных о голосовании
	 */
	private class ConnectAndUpdate implements Runnable
	{
		@Override
		public void run()
		{
			readPageFromServer();
			parsePageFromServer(true);
			parsePageFromServer(false);
			clean();
		}
	}
}