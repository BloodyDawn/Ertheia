/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.instancemanager.games;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class LotteryManager
{
	public static final long SECOND = 1000;
	public static final long MINUTE = 60000;

	protected static final Logger _log = LogManager.getLogger(LotteryManager.class);

	private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
	private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
	private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
	private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
	private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
	private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?";

	protected int _number;
	protected long _prize;
	protected boolean _isSellingTickets;
	protected boolean _isStarted;
	protected long _enddate;

	private LotteryManager()
	{
		_number = 1;
		_prize = Config.ALT_LOTTERY_PRIZE;
		_isSellingTickets = false;
		_isStarted = false;
		_enddate = System.currentTimeMillis();

		if(Config.ALLOW_LOTTERY)
		{
			new startLottery().run();
		}
	}

	public static LotteryManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public int getId()
	{
		return _number;
	}

	public long getPrize()
	{
		return _prize;
	}

	public long getEndDate()
	{
		return _enddate;
	}

	public void increasePrize(long count)
	{
		_prize += count;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_PRICE);
			statement.setLong(1, _prize);
			statement.setLong(2, _prize);
			statement.setInt(3, _number);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "LotteryManager: Could not increase current lottery prize: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public boolean isSellableTickets()
	{
		return _isSellingTickets;
	}

	public boolean isStarted()
	{
		return _isStarted;
	}

	public int[] decodeNumbers(int enchant, int type2)
	{
		int[] res = new int[5];
		int id = 0;
		int nr = 1;

		while(enchant > 0)
		{
			int val = enchant / 2;
			if(val != (double) enchant / 2)
			{
				res[id++] = nr;
			}
			enchant /= 2;
			nr++;
		}

		nr = 17;

		while(type2 > 0)
		{
			int val = type2 / 2;
			if(val != (double) type2 / 2)
			{
				res[id++] = nr;
			}
			type2 /= 2;
			nr++;
		}

		return res;
	}

	public long[] checkTicket(L2ItemInstance item)
	{
		return checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
	}

	public long[] checkTicket(int id, int enchant, int type2)
	{
		long[] res = {0, 0};

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_LOTTERY_TICKET);
			statement.setInt(1, id);
			rset = statement.executeQuery();

			if(rset.next())
			{
				int curenchant = rset.getInt("number1") & enchant;
				int curtype2 = rset.getInt("number2") & type2;

				if(curenchant == 0 && curtype2 == 0)
				{
					DatabaseUtils.closeResultSet(rset);
					DatabaseUtils.closeStatement(statement);
					return res;
				}

				int count = 0;

				for(int i = 1; i <= 16; i++)
				{
					int val = curenchant / 2;
					if(val != (double) curenchant / 2)
					{
						count++;
					}
					int val2 = curtype2 / 2;
					if(val2 != (double) curtype2 / 2)
					{
						count++;
					}
					curenchant = val;
					curtype2 = val2;
				}

				switch(count)
				{
					case 0:
						break;
					case 5:
						res[0] = 1;
						res[1] = rset.getLong("prize1");
						break;
					case 4:
						res[0] = 2;
						res[1] = rset.getLong("prize2");
						break;
					case 3:
						res[0] = 3;
						res[1] = rset.getLong("prize3");
						break;
					default:
						res[0] = 4;
						res[1] = 200;
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "count: " + count + ", id: " + id + ", enchant: " + enchant + ", type2: " + type2);
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "LotteryManager: Could not check lottery ticket #" + id + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		return res;
	}

	private static class SingletonHolder
	{
		protected static final LotteryManager _instance = new LotteryManager();
	}

	private class startLottery implements Runnable
	{
		protected startLottery()
		{
			// Do nothing
		}

		@Override
		public void run()
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(SELECT_LAST_LOTTERY);
				rset = statement.executeQuery();

				if(rset.next())
				{
					_number = rset.getInt("idnr");

					if(rset.getInt("finished") == 1)
					{
						_number++;
						_prize = rset.getLong("newprize");
					}
					else
					{
						_prize = rset.getLong("prize");
						_enddate = rset.getLong("enddate");

						if(_enddate <= System.currentTimeMillis() + 2 * MINUTE)
						{
							new finishLottery().run();
							DatabaseUtils.closeResultSet(rset);
							DatabaseUtils.closeStatement(statement);
							return;
						}

						if(_enddate > System.currentTimeMillis())
						{
							_isStarted = true;
							ThreadPoolManager.getInstance().scheduleGeneral(new finishLottery(), _enddate - System.currentTimeMillis());

							if(_enddate > System.currentTimeMillis() + 12 * MINUTE)
							{
								_isSellingTickets = true;
								ThreadPoolManager.getInstance().scheduleGeneral(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 10 * MINUTE);
							}
							DatabaseUtils.closeResultSet(rset);
							DatabaseUtils.closeStatement(statement);
							return;
						}
					}
				}
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "LotteryManager: Could not restore lottery data: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			}

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: Starting ticket sell for lottery #" + getId() + '.');
			}
			_isSellingTickets = true;
			_isStarted = true;

			Announcements.getInstance().announceToAll("LotteryManager tickets are now available for Lucky LotteryManager #" + getId() + '.');
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(_enddate);
			finishtime.set(Calendar.MINUTE, 0);
			finishtime.set(Calendar.SECOND, 0);

			if(finishtime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			{
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
				_enddate += 604800000;
			}
			else
			{
				finishtime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
			}

			ThreadPoolManager.getInstance().scheduleGeneral(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 10 * MINUTE);
			ThreadPoolManager.getInstance().scheduleGeneral(new finishLottery(), _enddate - System.currentTimeMillis());

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(INSERT_LOTTERY);
				statement.setInt(1, 1);
				statement.setInt(2, getId());
				statement.setLong(3, getEndDate());
				statement.setLong(4, getPrize());
				statement.setLong(5, getPrize());
				statement.execute();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "LotteryManager: Could not store new lottery data: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	private class stopSellingTickets implements Runnable
	{
		protected stopSellingTickets()
		{
			// Do nothing
		}

		@Override
		public void run()
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: Stopping ticket sell for lottery #" + getId() + '.');
			}
			_isSellingTickets = false;

			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_TEMP_SUSPENDED));
		}
	}

	private class finishLottery implements Runnable
	{
		protected finishLottery()
		{
			// Do nothing
		}

		@Override
		public void run()
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: Ending lottery #" + getId() + '.');
			}

			int[] luckynums = new int[5];
			int luckynum = 0;

			for(int i = 0; i < 5; i++)
			{
				boolean found = true;

				while(found)
				{
					luckynum = Rnd.get(20) + 1;
					found = false;

					for(int j = 0; j < i; j++)
					{
						if(luckynums[j] == luckynum)
						{
							found = true;
						}
					}
				}

				luckynums[i] = luckynum;
			}

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: The lucky numbers are " + luckynums[0] + ", " + luckynums[1] + ", " + luckynums[2] + ", " + luckynums[3] + ", " + luckynums[4] + '.');
			}

			int enchant = 0;
			int type2 = 0;

			for(int i = 0; i < 5; i++)
			{
				if(luckynums[i] < 17)
				{
					enchant += Math.pow(2, luckynums[i] - 1);
				}
				else
				{
					type2 += Math.pow(2, luckynums[i] - 17);
				}
			}

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: Encoded lucky numbers are " + enchant + ", " + type2);
			}

			int count1 = 0;
			int count2 = 0;
			int count3 = 0;
			int count4 = 0;

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(SELECT_LOTTERY_ITEM);
				statement.setInt(1, getId());
				rset = statement.executeQuery();

				while(rset.next())
				{
					int curenchant = rset.getInt("enchant_level") & enchant;
					int curtype2 = rset.getInt("custom_type2") & type2;

					if(curenchant == 0 && curtype2 == 0)
					{
						continue;
					}

					int count = 0;

					for(int i = 1; i <= 16; i++)
					{
						int val = curenchant / 2;

						if(val != (double) curenchant / 2)
						{
							count++;
						}

						int val2 = curtype2 / 2;

						if(val2 != (double) curtype2 / 2)
						{
							count++;
						}

						curenchant = val;
						curtype2 = val2;
					}

					if(count == 5)
					{
						count1++;
					}
					else if(count == 4)
					{
						count2++;
					}
					else if(count == 3)
					{
						count3++;
					}
					else if(count > 0)
					{
						count4++;
					}
				}
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "LotteryManager: Could restore lottery data: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			}

			long prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
			long prize1 = 0;
			long prize2 = 0;
			long prize3 = 0;

			if(count1 > 0)
			{
				prize1 = (long) ((getPrize() - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / count1);
			}

			if(count2 > 0)
			{
				prize2 = (long) ((getPrize() - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / count2);
			}

			if(count3 > 0)
			{
				prize3 = (long) ((getPrize() - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / count3);
			}

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: " + count1 + " players with all FIVE numbers each win " + prize1 + '.');
				_log.log(Level.DEBUG, "LotteryManager: " + count2 + " players with FOUR numbers each win " + prize2 + '.');
				_log.log(Level.DEBUG, "LotteryManager: " + count3 + " players with THREE numbers each win " + prize3 + '.');
				_log.log(Level.DEBUG, "LotteryManager: " + count4 + " players with ONE or TWO numbers each win " + prize4 + '.');
			}

			long newprize = getPrize() - (prize1 + prize2 + prize3 + prize4);
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "LotteryManager: Jackpot for next lottery is " + newprize + '.');
			}

			if(count1 > 0)
			{
				// There are winners.
				Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER).addNumber(getId()).addItemNumber(getPrize()).addItemNumber(count1));
			}
			else
			{
				// There are no winners.
				Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER).addNumber(getId()).addItemNumber(getPrize()));
			}

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(UPDATE_LOTTERY);
				statement.setLong(1, getPrize());
				statement.setLong(2, newprize);
				statement.setInt(3, enchant);
				statement.setInt(4, type2);
				statement.setLong(5, prize1);
				statement.setLong(6, prize2);
				statement.setLong(7, prize3);
				statement.setInt(8, getId());
				statement.execute();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "LotteryManager: Could not store finished lottery data: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}

			ThreadPoolManager.getInstance().scheduleGeneral(new startLottery(), MINUTE);
			_number++;

			_isStarted = false;
		}
	}
}
