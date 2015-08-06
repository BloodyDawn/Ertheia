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
package dwo.gameserver;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.datatables.sql.AccountShareDataTable;
import dwo.gameserver.datatables.sql.ChaosFestivalTable;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.OfflineTradersTable;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.instancemanager.GlobalVariablesManager;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.instancemanager.ItemAuctionManager;
import dwo.gameserver.instancemanager.ItemsOnGroundManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.RaidBossSpawnManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SeverClose;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.login.gameserverpackets.ServerStatus;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.database.DatabaseBackupManager;
import gnu.trove.procedure.TObjectProcedure;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This class provides the functions for shutting down and restarting the server
 * It closes all open clientconnections and saves all data.
 *
 * @version $Revision: 1.2.4.5 $ $Date: 2005/03/27 15:29:09 $
 */
public class GameServerShutdown extends Thread
{
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT = {"SIGTERM", "shutting down", "restarting", "aborting"};
	private static Logger _log = LogManager.getLogger(GameServerShutdown.class);
	private static GameServerShutdown _counterInstance;
	private int _secondsShut;
	private int _shutdownMode;

	/**
	 * Default constucter is only used internal to create the shutdown-hook instance
	 */
	private GameServerShutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}

	/**
	 * This creates a countdown instance of GameServerShutdown.
	 *
	 * @param seconds how many seconds until shutdown
	 * @param restart true is the server shall restart after shutdown
	 */
	public GameServerShutdown(int seconds, boolean restart)
	{
		if(seconds < 0)
		{
			seconds = 0;
		}
		_secondsShut = seconds;
		_shutdownMode = restart ? GM_RESTART : GM_SHUTDOWN;
	}

	/**
	 * get the shutdown-hook instance
	 * the shutdown-hook instance is created by the first call of this function,
	 * but it has to be registrered externaly.
	 *
	 * @return instance of GameServerShutdown, to be used as shutdown hook
	 */
	public static GameServerShutdown getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * this disconnects all clients from the server
	 */
	private static void disconnectAllCharacters()
	{
		WorldManager.getInstance().getAllPlayers().safeForEachValue(new disconnectAllCharacters());
	}

	/**
	 * This function starts a shutdown countdown from Telnet (Copied from Function startShutdown())
	 *
	 * @param seconds seconds untill shutdown
	 */
	private void SendServerQuit(int seconds)
	{
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(seconds));
	}

	/**
	 * This function starts a shutdown countdown from Telnet (Copied from Function startShutdown())
	 *
	 * @param IP      IP Which Issued shutdown command
	 * @param seconds seconds untill shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	public void startTelnetShutdown(String IP, int seconds, boolean restart)
	{
		_log.log(Level.WARN, "IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		//_an.announceToAll("Server is " + _modeText[shutdownMode] + " in "+seconds+ " seconds!");

		_shutdownMode = restart ? GM_RESTART : GM_SHUTDOWN;

		if(_shutdownMode > 0)
		{
			switch(seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}

		if(_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new GameServerShutdown(seconds, restart);
		_counterInstance.start();
	}

	/**
	 * This function aborts a running countdown
	 *
	 * @param IP IP Which Issued shutdown command
	 */
	public void telnetAbort(String IP)
	{
		_log.log(Level.WARN, "IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");

		if(_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		}
	}

	/**
	 * this function is called, when a new thread starts
	 * <p/>
	 * if this thread is the thread of getInstance, then this is the shutdown hook
	 * and we save all data and disconnect all clients.
	 * <p/>
	 * after this thread ends, the server will completely exit
	 * <p/>
	 * if this is not the thread of getInstance, then this is a countdown thread.
	 * we start the countdown, and when we finished it, and it was not aborted,
	 * we tell the shutdown-hook why we call exit, and then call exit
	 * <p/>
	 * when the exit status of the server is 1, startServer.sh / startServer.bat
	 * will restart the server.
	 */
	@Override
	public void run()
	{
		if(equals(SingletonHolder._instance))
		{
			TimeCounter tc = new TimeCounter();
			TimeCounter tc1 = new TimeCounter();
			try
			{
				if((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
				{
					OfflineTradersTable.storeOffliners();
					_log.log(Level.INFO, "Offline Traders Table: Offline shops stored(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
				}
			}
			catch(Throwable t)
			{
				_log.log(Level.WARN, "Error saving offline shops.", t);
			}

			try
			{
				disconnectAllCharacters();
				_log.log(Level.INFO, "All players disconnected and saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch(Throwable t)
			{
				// ignore
			}

			// ensure all services are stopped
			try
			{
				GameTimeController.getInstance().stopTimer();
				_log.log(Level.INFO, "Game Time Controller: Timer stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch(Throwable t)
			{
				// ignore
			}

			// stop all threadpolls
			try
			{
				ThreadPoolManager.getInstance().shutdown();
				_log.log(Level.INFO, "Thread Pool Manager: Manager has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch(Throwable t)
			{
				// ignore
			}

			try
			{
				LoginServerThread.getInstance().interrupt();
				_log.log(Level.INFO, "Login Server Thread: Thread interruped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch(Throwable t)
			{
				// ignore
			}

			// last byebye, save all data and quit this server
			saveData();
			tc.restartCounter();

			// saveData sends messages to exit players, so shutdown selector after it
			try
			{
				GameServerStartup.gameServer.getSelectorThread().shutdown();
				_log.log(Level.INFO, "Game Server: Selector thread has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch(Throwable t)
			{
				// ignore
			}

			// commit data, last chance
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
				_log.log(Level.INFO, "L2Database Factory: Database connection has been shutdown (" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch(Throwable t)
			{
				_log.log(Level.ERROR, "Error while closing SQL connection!", t);
			}

			// server will quit, when this function ends.
			if(SingletonHolder._instance._shutdownMode == GM_RESTART)
			{
				Runtime.getRuntime().halt(2);
			}
			else
			{
				Runtime.getRuntime().halt(0);
			}

			_log.log(Level.INFO, "The server has been successfully shut down in " + tc1.getEstimatedTime() / 1000 + "seconds.");
		}
		else
		{
			// gm shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			// last point where logging is operational :(
			_log.log(Level.WARN, "GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch(_shutdownMode)
			{
				case GM_SHUTDOWN:
					SingletonHolder._instance._shutdownMode = GM_SHUTDOWN;
					System.exit(0);
					break;
				case GM_RESTART:
					SingletonHolder._instance._shutdownMode = GM_RESTART;
					System.exit(2);
					break;
			}
		}
	}

	/**
	 * This functions starts a shutdown countdown
	 *
	 * @param charName GM who issued the shutdown command
	 * @param seconds    seconds until shutdown
	 * @param restart    true if the server will restart after shutdown
	 */
	public void startShutdown(String charName, int seconds, boolean restart)
	{
		_shutdownMode = restart ? GM_RESTART : GM_SHUTDOWN;

		_log.log(Level.WARN, "GM: " + charName + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");

		if(_shutdownMode > 0)
		{
			switch(seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}

		if(_counterInstance != null)
		{
			_counterInstance._abort();
		}

		// the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new GameServerShutdown(seconds, restart);
		_counterInstance.start();
	}

	/**
	 * This function aborts a running countdown
	 *
	 * @param charName GM who issued the abort command
	 */
	public void abort(String charName)
	{
		_log.log(Level.WARN, "GM: " + charName + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		if(_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		}
	}

	/**
	 * set the shutdown mode
	 *
	 * @param mode what mode shall be set
	 */
	private void setMode(int mode)
	{
		_shutdownMode = mode;
	}

	/**
	 * set shutdown mode to ABORT
	 */
	private void _abort()
	{
		_shutdownMode = ABORT;
	}

	public int getShutdownMode()
	{
		return _shutdownMode;
	}

	public int getSecondsShut()
	{
		return _secondsShut;
	}

	/**
	 * this counts the countdown and reports it to all players
	 * countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		try
		{
			while(_secondsShut > 0)
			{
				switch(_secondsShut)
				{
					case 540:
						SendServerQuit(540);
						break;
					case 480:
						SendServerQuit(480);
						break;
					case 420:
						SendServerQuit(420);
						break;
					case 360:
						SendServerQuit(360);
						break;
					case 300:
						SendServerQuit(300);
						break;
					case 240:
						SendServerQuit(240);
						break;
					case 180:
						SendServerQuit(180);
						break;
					case 120:
						SendServerQuit(120);
						break;
					case 60:
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN); //avoids new players from logging in
						SendServerQuit(60);
						break;
					case 30:
						SendServerQuit(30);
						break;
					case 10:
						SendServerQuit(10);
						break;
					case 5:
						SendServerQuit(5);
						break;
					case 4:
						SendServerQuit(4);
						break;
					case 3:
						SendServerQuit(3);
						break;
					case 2:
						SendServerQuit(2);
						break;
					case 1:
						SendServerQuit(1);
						break;
				}

				_secondsShut--;

				int delay = 1000; //milliseconds
				Thread.sleep(delay);

				if(_shutdownMode == ABORT)
				{
					break;
				}
			}
		}
		catch(InterruptedException e)
		{
			//this will never happen
		}
	}

	/**
	 * this sends a last byebye, disconnects all players and saves data
	 */
	private void saveData()
	{
		switch(_shutdownMode)
		{
			case SIGTERM:
				_log.log(Level.INFO, "SIGTERM received. Shutting down NOW!");
				break;
			case GM_SHUTDOWN:
				_log.log(Level.INFO, "GM shutdown received. Shutting down NOW!");
				break;
			case GM_RESTART:
				_log.log(Level.INFO, "GM restart received. Restarting NOW!");
				break;

		}

		if(Config.DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN)
		{
			DatabaseBackupManager.makeBackup();
		}
		TimeCounter tc = new TimeCounter();

		// Save all raidboss and GrandBoss status ^_^
		RaidBossSpawnManager.getInstance().cleanUp();
		_log.log(Level.INFO, "RaidBossSpawnManager: All raidboss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		GrandBossManager.getInstance().cleanUp();
		_log.log(Level.INFO, "GrandBossManager: All Grand Boss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		HellboundManager.getInstance().cleanUp();
		_log.log(Level.INFO, "Hellbound Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		_log.log(Level.INFO, "TradeController saving data.. This action may take some minutes! Please wait until completed!");
		// TODO: BuylistTable.getInstance().dataCountStore();
		_log.log(Level.INFO, "TradeController: All count Item saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		ItemAuctionManager.getInstance().shutdown();
		_log.log(Level.INFO, "Item ClanHallAuctionEngine Manager: All tasks stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		Olympiad.getInstance().saveOlympiadStatus();
		_log.log(Level.INFO, "Olympiad System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		HeroManager.getInstance().shutdown();
		_log.log(Level.INFO, "Hero System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		ClanTable.getInstance().storeClanScore();
		_log.log(Level.INFO, "Clan System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");

		// Save Cursed Weapons data before closing.
		CursedWeaponsManager.getInstance().saveData();
		_log.log(Level.INFO, "Cursed Weapons Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");

		// Save all manor data
		CastleManorManager.getInstance().save();
		_log.log(Level.INFO, "Castle Manor Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");

		// Сохраняем состояние осад кланхоллов
		ClanHallSiegeManager.getInstance().onServerShutDown();
		_log.log(Level.INFO, "CHSiegeManager: Siegable hall attacker lists saved!");

		// Save all global (non-player specific) Quest data that needs to persist after reboot
		QuestManager.getInstance().save();
		_log.log(Level.INFO, "Quest Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");

		// Save all global variables data
		GlobalVariablesManager.getInstance().saveVars();
		_log.log(Level.INFO, "Global Variables Manager: Variables saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");

		// Saving account data
		AccountShareDataTable.getInstance().updateInDb();

		// Сохраняем мировую статистику
		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			WorldStatisticsManager.getInstance().updateAllStatsInDb();
		}

		ChaosFestivalTable.getInstance().cleanUp();

		ClanSearchManager.getInstance().shutdown();

		//Save items on ground before closing
		if(Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			_log.log(Level.INFO, "Items On Ground Manager: Data saved (" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		}

		try
		{
			int delay = 5000;
			Thread.sleep(delay);
		}
		catch(InterruptedException e)
		{
			//never happens :p
		}
	}

	public void startConsoleShutdown(String ident, int seconds, boolean restart)
	{
		startShutdown(null, seconds, restart);
		_log.log(Level.WARN, "[Console]: '" + ident + "' issued shutdown. " + (restart ? "Restart" : "Shutdown") + " in " + seconds / 60 + " minute(s) and " + seconds % 60 + " second(s)!");
	}

	public void abortConsoleShutdown(String ident)
	{
		if(_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		}

		if(_shutdownMode == GM_RESTART)
		{
			_log.log(Level.WARN, "[Console]: '" + ident + "' aborted restart!");
			Broadcast.announceToOnlinePlayers("Server restart has been aborted. Continuing normal operations.");
		}
		else
		{
			_log.log(Level.WARN, "[Console]: '" + ident + "' aborted shutdown!");
			Broadcast.announceToOnlinePlayers("Server shutdown has been aborted. Continuing normal operations.");
		}
	}

	private static class disconnectAllCharacters implements TObjectProcedure<L2PcInstance>
	{
		@Override
		public boolean execute(L2PcInstance player)
		{
			if(player != null)
			{
				//Logout Character
				try
				{
					L2GameClient client = player.getClient();
					if(client != null && !client.isDetached())
					{
						client.close(SeverClose.STATIC_PACKET);
						client.setActiveChar(null);
						player.setClient(null);
					}
					player.getLocationController().delete();
				}
				catch(Throwable t)
				{
					_log.log(Level.WARN, "Failed logour char " + player, t);
				}
			}
			return true;
		}
	}

	/**
	 * A simple class used to track down the estimated time of method executions.
	 * Once this class is created, it saves the start time, and when you want to get
	 * the estimated time, use the getEstimatedTime() method.
	 */
	private static class TimeCounter
	{
		private long _startTime;

		private TimeCounter()
		{
			restartCounter();
		}

		private void restartCounter()
		{
			_startTime = System.currentTimeMillis();
		}

		private long getEstimatedTimeAndRestartCounter()
		{
			long toReturn = System.currentTimeMillis() - _startTime;
			restartCounter();
			return toReturn;
		}

		private long getEstimatedTime()
		{
			return System.currentTimeMillis() - _startTime;
		}
	}

	private static class SingletonHolder
	{
		protected static final GameServerShutdown _instance = new GameServerShutdown();
	}
}
