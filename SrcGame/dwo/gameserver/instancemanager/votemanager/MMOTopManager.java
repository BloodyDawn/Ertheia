package dwo.gameserver.instancemanager.votemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.StringTokenizer;

public class MMOTopManager
{
	private static final Logger _log = LogManager.getLogger("vote");

	private static final String SELECT_PLAYER_OBJID = "SELECT charId FROM characters WHERE char_name=?";
	private static final String SELECT_CHARACTER_MMOTOP_DATA = "SELECT * FROM character_vote_mmotop WHERE id=? AND date=? AND multipler=?";
	private static final String INSERT_MMOTOP_DATA = "INSERT INTO character_vote_mmotop (date, id, nick, multipler) values (?,?,?,?)";
	private static final String DELETE_MMOTOP_DATA = "DELETE FROM character_vote_mmotop WHERE date<?";
	private static final String SELECT_MULTIPLER_MMOTOP_DATA = "SELECT multipler FROM character_vote_mmotop WHERE id=? AND has_reward=0";
	private static final String UPDATE_MMOTOP_DATA = "UPDATE character_vote_mmotop SET has_reward=1 WHERE id=?";
	private static MMOTopManager _instance;
	BufferedReader reader;

	public MMOTopManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConnectAndUpdate(), Config.MMO_TOP_MANAGER_INTERVAL, Config.MMO_TOP_MANAGER_INTERVAL);
	}

	public static MMOTopManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new MMOTopManager();
		}
		return _instance;
	}

	public void getPage(String address)
	{
		try
		{
			URL url = new URL(address);
			reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		}
		catch(Exception e)
		{
			_log.log(Level.INFO, "MMOTOP: Server didn't response. ");
		}
	}

	public void parse()
	{
		try
		{
			String line;
			while(reader != null && (line = reader.readLine()) != null)
			{
				if(line.startsWith("Нет данных") || line.startsWith("QRATOR HTTP") || line.contains("Попробуйте обновить страницу"))
				{
					break;
				}

				StringTokenizer st = new StringTokenizer(line, "\t. :");
				while(st.hasMoreTokens())
				{
					try
					{
						st.nextToken();
						int day = Integer.parseInt(st.nextToken());
						int month = Integer.parseInt(st.nextToken()) - 1;
						int year = Integer.parseInt(st.nextToken());
						int hour = Integer.parseInt(st.nextToken());
						int minute = Integer.parseInt(st.nextToken());
						int second = Integer.parseInt(st.nextToken());
						st.nextToken();
						st.nextToken();
						st.nextToken();
						st.nextToken();
						String charName = st.nextToken();
						int voteType = 0;
						if(st.hasMoreTokens())
						{
							voteType = Integer.parseInt(st.nextToken());
						}
						else
						{
							continue;
						}

						Calendar calendar = Calendar.getInstance();
						calendar.set(1, year);
						calendar.set(2, month);
						calendar.set(5, day);
						calendar.set(11, hour);
						calendar.set(12, minute);
						calendar.set(13, second);
						calendar.set(14, 0);

						long voteTime = calendar.getTimeInMillis() / 1000;

						if(voteTime + Config.MMO_TOP_SAVE_DAYS * 86400 > System.currentTimeMillis() / 1000)
						{
							checkAndSave(voteTime, charName, voteType);
						}
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while parse() line: " + line + " e:", e);
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while parse()", e);
		}
		finally
		{
			clean();
		}
	}

	public void checkAndSave(long voteTime, String charName, int voteType)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement selectObjectStatement = null;
		FiltredPreparedStatement selectMmotopStatement = null;
		FiltredPreparedStatement insertStatement = null;
		ResultSet rsetObject = null;
		ResultSet rsetMmotop = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			selectObjectStatement = con.prepareStatement(SELECT_PLAYER_OBJID);
			selectObjectStatement.setString(1, charName);
			rsetObject = selectObjectStatement.executeQuery();

			int objId = 0;
			if(rsetObject.next())
			{
				objId = rsetObject.getInt("charId");
			}
			if(objId > 0)
			{
				selectMmotopStatement = con.prepareStatement(SELECT_CHARACTER_MMOTOP_DATA);
				selectMmotopStatement.setInt(1, objId);
				selectMmotopStatement.setLong(2, voteTime);
				selectMmotopStatement.setInt(3, voteType);
				rsetMmotop = selectMmotopStatement.executeQuery();
				if(!rsetMmotop.next())
				{
					insertStatement = con.prepareStatement(INSERT_MMOTOP_DATA);
					insertStatement.setLong(1, voteTime);
					insertStatement.setInt(2, objId);
					insertStatement.setString(3, charName);
					insertStatement.setInt(4, voteType);
					insertStatement.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while checkAndSave()", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, selectObjectStatement, rsetObject);
			DatabaseUtils.closeDatabaseSR(selectMmotopStatement, rsetMmotop);
			DatabaseUtils.closeStatement(insertStatement);
		}
	}

	private void clean()
	{
		synchronized(this)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -Config.MMO_TOP_SAVE_DAYS);
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(DELETE_MMOTOP_DATA);
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
					selectMultStatement = con.prepareStatement(SELECT_MULTIPLER_MMOTOP_DATA);
					selectMultStatement.setInt(1, objId);
					rsetMult = selectMultStatement.executeQuery();

					while(rsetMult.next())
					{
						mult += rsetMult.getInt("multipler");
					}

					if(mult > 0)
					{
						updateStatement = con.prepareStatement(UPDATE_MMOTOP_DATA);
						updateStatement.setInt(1, objId);
						updateStatement.executeUpdate();

						if(player.getLang().equals("ru"))
						{
							player.sendMessage("Спасибо за Ваш голос в рейтинге MMOTop. C наилучшими пожеланиями, Администрация сервера.");
						}
						else
						{
							player.sendMessage("Thank you for your vote in MMOTop raiting. Best regards, Administration. ");
						}

						if(Config.MMO_TOP_REWARD[0] == -100) // PC Bang
						{
							player.setPcBangPoints(player.getPcBangPoints() + Config.MMO_TOP_REWARD[1] * mult);
							_log.log(Level.INFO, "MMOTOP: " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.MMO_TOP_REWARD[0] + "count:" + Config.MMO_TOP_REWARD[1] * mult + ']');
						}
						else if(Config.MMO_TOP_REWARD[0] == -200) // Clan reputation
						{
							if(player.getClan() != null)
							{
								player.getClan().addReputationScore(Config.MMO_TOP_REWARD[1] * mult, true);
								_log.log(Level.INFO, "MMOTOP: " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.MMO_TOP_REWARD[0] + "count:" + Config.MMO_TOP_REWARD[1] * mult + ']');
							}
							else
							{
								L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.DONATION, Config.MMO_TOP_REWARD_NO_CLAN[0], Config.MMO_TOP_REWARD_NO_CLAN[1] * mult, null);
								player.addItem(ProcessType.DONATION, item, null, true);
								_log.log(Level.INFO, "MMOTOP: " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.MMO_TOP_REWARD_NO_CLAN[0] + "count:" + Config.MMO_TOP_REWARD_NO_CLAN[1] * mult + ']');
							}
						}
						else if(Config.MMO_TOP_REWARD[0] == -300) // Fame
						{
							player.setFame(player.getFame() + Config.MMO_TOP_REWARD[1] * mult);
							_log.log(Level.INFO, "MMOTOP: " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.MMO_TOP_REWARD[0] + "count:" + Config.MMO_TOP_REWARD[1] * mult + ']');
						}
						else
						{
							L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.DONATION, Config.MMO_TOP_REWARD[0], Config.MMO_TOP_REWARD[1] * mult, null);
							player.addItem(ProcessType.DONATION, item, null, true);
							_log.log(Level.INFO, "MMOTOP: " + player.getName() + "[obj:" + player.getObjectId() + "]  item: [id:" + Config.MMO_TOP_REWARD[0] + "count:" + Config.MMO_TOP_REWARD[1] * mult + ']');
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while giveReward()", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, selectMultStatement, rsetMult);
				DatabaseUtils.closeStatement(updateStatement);
			}
		}
	}

	private class ConnectAndUpdate implements Runnable
	{
		@Override
		public void run()
		{
			getPage(Config.MMO_TOP_WEB_ADDRESS);
			parse();
		}
	}
}