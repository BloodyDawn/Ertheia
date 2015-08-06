package dwo.gameserver.model.world.olympiad;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.logengine.formatters.OlympiadLogFormatter;
import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.arrays.L2FastList;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

public class Olympiad
{
	public static final String OLYMPIAD_HTML_PATH = "olympiad/";
	public static final String CHAR_ID = "charId";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_DONE_WEEKLY = "competitions_done_weekly";
	public static final String COMP_DONE_WEEKLY_CLASSED = "competitions_done_weekly_c";
	public static final String COMP_DONE_WEEKLY_NON_CLASSED = "competitions_done_weekly_nc";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	protected static final Logger _log = LogManager.getLogger(Olympiad.class);
	protected static final Logger _logResults = LogManager.getLogger("olympiad");
	protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
	protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours
	protected static final int DEFAULT_POINTS = Config.ALT_OLY_START_POINTS;
	protected static final int WEEKLY_POINTS = Config.ALT_OLY_WEEKLY_POINTS;
	private static final Map<Integer, StatsSet> _nobles = new FastMap<>();
	private static final TIntIntHashMap _noblesRank = new TIntIntHashMap();
	private static final String OLYMPIAD_DATA_FILE = "config/main/Olympiad.ini";
	private static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle, period, week, olympiad_end, validation_end, " + "next_weekly_change FROM olympiad_data WHERE id = 0";
	private static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle, period, " + "week, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE current_cycle=?, period=?, week=?, olympiad_end=?, " + "validation_end=?, next_weekly_change=?";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, " + "characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done," + "olympiad_nobles.competitions_done_weekly, olympiad_nobles.competitions_done_weekly_c, olympiad_nobles.competitions_done_weekly_nc, " + "olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn " + "FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles " + "(`charId`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_done_weekly`,`competitions_done_weekly_c`,`competitions_done_weekly_nc`,`competitions_won`,`competitions_lost`," + "`competitions_drawn`) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET " + "olympiad_points = ?, competitions_done = ?, competitions_done_weekly=?, competitions_done_weekly_c = ?, competitions_done_weekly_nc = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE charId = ?";
	private static final String OLYMPIAD_GET_HEROES = "SELECT olympiad_nobles.charId, characters.char_name " + "FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId " + "AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
	private static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT charId from olympiad_nobles_eom " + "WHERE competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters " + "WHERE characters.charId = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? " + "AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + ' ' + "ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT * FROM olympiad_nobles";
	private static final int[] HERO_IDS = {
		148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169,
		170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 188, 189
	};
	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 6PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 6 hours
	protected static L2FastList<StatsSet> _heroesToBe;
	protected static boolean _inCompPeriod;
	protected static boolean _compStarted;
	protected long _olympiadEnd;
	protected long _validationEnd;
	/**
	 * The current period of the olympiad.<br>
	 * <b>0 -</b> Competition period<br>
	 * <b>1 -</b> Validation Period
	 */
	protected int _period;
	protected int _week;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValdationTask;
	protected ScheduledFuture<?> _gameManager;
	protected ScheduledFuture<?> _gameAnnouncer;
	private long _compEnd;
	private Calendar _compStart;

	private Olympiad()
	{
		load();

		if(_period == 0)
		{
			init();
		}
	}

	public static Olympiad getInstance()
	{
		return SingletonHolder._instance;
	}

	protected static int getNobleCount()
	{
		return _nobles.size();
	}

	/**
	 * @param charId the noble object Id.
	 * @param data the stats set data to add.
	 * @return the old stats set if the noble is already present, null otherwise.
	 */
	protected static StatsSet addNobleStats(int charId, StatsSet data)
	{
		return _nobles.put(charId, data);
	}

	private void load()
	{
		_nobles.clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		boolean loaded = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OLYMPIAD_LOAD_DATA);
			rset = statement.executeQuery();

			while(rset.next())
			{
				_currentCycle = rset.getInt("current_cycle");
				_period = rset.getInt("period");
				_week = rset.getInt("week");
				_olympiadEnd = rset.getLong("olympiad_end");
				_validationEnd = rset.getLong("validation_end");
				_nextWeeklyChange = rset.getLong("next_weekly_change");
				loaded = true;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Olympiad System: Error loading olympiad data from database: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		if(!loaded)
		{
			_log.log(Level.INFO, "Olympiad System: failed to load data from database, trying to load from file.");

			Properties OlympiadProperties = new Properties();
			InputStream is = null;
			try
			{
				is = new FileInputStream(new File("./" + OLYMPIAD_DATA_FILE));
				OlympiadProperties.load(is);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Olympiad System: Error loading olympiad properties: ", e);
				return;
			}
			finally
			{
				try
				{
					is.close();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "", e);
				}
			}

			_currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
			_period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
			_week = Integer.parseInt(OlympiadProperties.getProperty("Week", "1"));
			_olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
			_validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValidationEnd", "0"));
			_nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));
		}

		switch(_period)
		{
			case 0:
				if(_olympiadEnd == 0 || _olympiadEnd < Calendar.getInstance().getTimeInMillis())
				{
					setNewOlympiadEnd();
				}
				else
				{
					scheduleWeeklyChange();
				}
				break;
			case 1:
				if(_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					loadNoblesRank();
					_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
				}
				else
				{
					_currentCycle++;
					_period = 0;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
			default:
				_log.log(Level.WARN, "Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			rset = statement.executeQuery();
			StatsSet statData;
			while(rset.next())
			{
				statData = new StatsSet();
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_DONE_WEEKLY, rset.getInt(COMP_DONE_WEEKLY));
				statData.set(COMP_DONE_WEEKLY_CLASSED, rset.getInt(COMP_DONE_WEEKLY_CLASSED));
				statData.set(COMP_DONE_WEEKLY_NON_CLASSED, rset.getInt(COMP_DONE_WEEKLY_NON_CLASSED));
				statData.set("to_save", false);

				addNobleStats(rset.getInt(CHAR_ID), statData);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Olympiad System: Error loading noblesse data from database: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		synchronized(this)
		{
			_log.log(Level.INFO, "Olympiad System: Loading Olympiad System....");
			if(_period == 0)
			{
				_log.log(Level.INFO, "Olympiad System: Currently in Olympiad Period");
			}
			else
			{
				_log.log(Level.INFO, "Olympiad System: Currently in Validation Period");
			}

			long milliToEnd;
			milliToEnd = _period == 0 ? getMillisToOlympiadEnd() : getMillisToValidationEnd();

			_log.log(Level.INFO, "Olympiad System: Now " + _week + " week of Olympiad Competitions.");
			_log.log(Level.INFO, "Olympiad System: " + Math.round(milliToEnd / 60000) + " minutes until period ends");

			if(_period == 0)
			{
				milliToEnd = getMillisToWeekChange();

				_log.log(Level.INFO, "Olympiad System: Next weekly change is in " + Math.round(milliToEnd / 60000) + " minutes");
			}
		}

		_log.log(Level.INFO, "Olympiad System: Loaded " + _nobles.size() + " Nobles");

	}

	public void loadNoblesRank()
	{
		_noblesRank.clear();
		TIntIntHashMap tmpPlace = new TIntIntHashMap();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS);
			rset = statement.executeQuery();

			int place = 1;
			while(rset.next())
			{
				tmpPlace.put(rset.getInt(CHAR_ID), place++);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Olympiad System: Error loading noblesse data from database for Ranking: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.10);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.50);
		if(rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}
		for(int charId : tmpPlace.keys())
		{
			if(tmpPlace.get(charId) <= rank1)
			{
				_noblesRank.put(charId, 1);
			}
			else if(tmpPlace.get(charId) <= rank2)
			{
				_noblesRank.put(charId, 2);
			}
			else if(tmpPlace.get(charId) <= rank3)
			{
				_noblesRank.put(charId, 3);
			}
			else if(tmpPlace.get(charId) <= rank4)
			{
				_noblesRank.put(charId, 4);
			}
			else
			{
				_noblesRank.put(charId, 5);
			}
		}
	}

	protected void init()
	{
		if(_period == 1)
		{
			return;
		}

		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;

		if(_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}

		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), getMillisToOlympiadEnd());

		updateCompStatus();
	}

	public StatsSet getNobleStats(L2PcInstance noble)
	{
		return _nobles.get(noble.getObjectId());
	}

	public void generateNobleStats(L2PcInstance noble)
	{
		if(!noble.isNoble())
		{
			return;
		}
		StatsSet nobleStats = _nobles.get(noble.getObjectId());
		if(nobleStats == null)
		{
			nobleStats = new StatsSet();
			nobleStats.set(CLASS_ID, noble.getBaseClassId());
			nobleStats.set(CHAR_NAME, noble.getName());
			nobleStats.set(POINTS, DEFAULT_POINTS);
			nobleStats.set(COMP_DONE, 0);
			nobleStats.set(COMP_DONE_WEEKLY, 0);
			nobleStats.set(COMP_DONE_WEEKLY_CLASSED, 0);
			nobleStats.set(COMP_DONE_WEEKLY_NON_CLASSED, 0);
			nobleStats.set(COMP_WON, 0);
			nobleStats.set(COMP_LOST, 0);
			nobleStats.set(COMP_DRAWN, 0);
			nobleStats.set("to_save", true);
			addNobleStats(noble.getObjectId(), nobleStats);
		}
	}

	private void updateCompStatus()
	{
		synchronized(this)
		{
			long milliToStart = getMillisToCompBegin();

			double numSecs = milliToStart / 1000 % 60;
			double countDown = (milliToStart / 1000.0 - numSecs) / 60;
			int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			int numHours = (int) Math.floor(countDown % 24);
			int numDays = (int) Math.floor((countDown - numHours) / 24);

			_log.log(Level.INFO, "Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

			_log.log(Level.INFO, "Olympiad System: Event starts/started : " + _compStart.getTime());
		}

		_scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(isOlympiadEnd())
			{
				return;
			}

			_inCompPeriod = true;

			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
			_log.log(Level.INFO, "Olympiad System: Olympiad Game STARTED");

			_gameManager = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(OlympiadGameManager.getInstance(), 30000, 30000);
			if(Config.ALT_OLY_ANNOUNCE_GAMES)
			{
				_gameAnnouncer = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new OlympiadAnnouncer(), 30000, 500);
			}

			long regEnd = getMillisToCompEnd() - 600000;
			if(regEnd > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED)), regEnd);
			}

			_scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
				if(isOlympiadEnd())
				{
					return;
				}
				_inCompPeriod = false;
				Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
				_log.log(Level.INFO, "Olympiad System: Olympiad Game Ended");

				while(OlympiadGameManager.getInstance().isBattleStarted()) // cleared in game manager
				{
					try
					{
						// wait 1 minutes for end of pendings games
						Thread.sleep(60000);
					}
					catch(InterruptedException e)
					{
						_log.log(Level.ERROR, "", e);
					}
				}

				if(_gameManager != null)
				{
					_gameManager.cancel(false);
					_gameManager = null;
				}

				if(_gameAnnouncer != null)
				{
					_gameAnnouncer.cancel(false);
					_gameAnnouncer = null;
				}

				saveOlympiadStatus();

				init();
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}

	private long getMillisToOlympiadEnd()
	{
		return _olympiadEnd - Calendar.getInstance().getTimeInMillis();
	}

	public void manualSelectHeroes()
	{
		if(_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}

		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), 0);
	}

	protected long getMillisToValidationEnd()
	{
		if(_validationEnd > Calendar.getInstance().getTimeInMillis())
		{
			return _validationEnd - Calendar.getInstance().getTimeInMillis();
		}
		return 10L;
	}

	public boolean isOlympiadEnd()
	{
		return _period != 0;
	}

	protected void setNewOlympiadEnd()
	{
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
		sm.addNumber(_currentCycle);

		Announcements.getInstance().announceToAll(sm);

		Calendar currentTime = Calendar.getInstance();
		currentTime.add(Calendar.MONTH, 1);
		currentTime.set(Calendar.DAY_OF_MONTH, 1);
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		_olympiadEnd = currentTime.getTimeInMillis();

		Calendar nextChange = Calendar.getInstance();
		_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		scheduleWeeklyChange();
	}

	public boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private long getMillisToCompBegin()
	{
		if(_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _compEnd > Calendar.getInstance().getTimeInMillis())
		{
			return 10L;
		}

		if(_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		{
			return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		}

		return setNewCompBegin();
	}

	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;

		_log.log(Level.INFO, "Olympiad System: New Schedule @ " + _compStart.getTime());

		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	protected long getMillisToCompEnd()
	{
		return _compEnd - Calendar.getInstance().getTimeInMillis();
	}

	private long getMillisToWeekChange()
	{
		if(_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
		{
			return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
		}
		return 10L;
	}

	private void scheduleWeeklyChange()
	{
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			addWeeklyPoints();
			_log.log(Level.INFO, "Olympiad System: Added weekly points to nobles");
			resetWeeklyMatches();
			_log.log(Level.INFO, "Olympiad System: Reset weekly matches to nobles");

			Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}, getMillisToWeekChange(), WEEKLY_PERIOD);
	}

	protected void addWeeklyPoints()
	{
		synchronized(this)
		{
			if(_period == 1)
			{
				return;
			}

			int currentPoints;
			for(StatsSet nobleInfo : _nobles.values())
			{
				currentPoints = nobleInfo.getInteger(POINTS);
				currentPoints += WEEKLY_POINTS;
				nobleInfo.set(POINTS, currentPoints);
			}
		}
	}

	public int getCurrentCycle()
	{
		return _currentCycle;
	}

	public boolean playerInStadia(L2PcInstance player)
	{
		return ZoneManager.getInstance().getOlympiadStadium(player) != null;
	}

	/**
	 * Save noblesse data to database
	 */
	protected void saveNobleData()
	{
		synchronized(this)
		{
			if(_nobles == null || _nobles.isEmpty())
			{
				return;
			}
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				for(Entry<Integer, StatsSet> entry : _nobles.entrySet())
				{
					StatsSet nobleInfo = entry.getValue();

					if(nobleInfo == null)
					{
						continue;
					}
					int charId = entry.getKey();
					int classId = nobleInfo.getInteger(CLASS_ID);
					int points = nobleInfo.getInteger(POINTS);
					int compDone = nobleInfo.getInteger(COMP_DONE);
					int competitionsDoneWeekly = nobleInfo.getInteger(COMP_DONE_WEEKLY);
					int competitionsDoneWeeklyClassed = nobleInfo.getInteger(COMP_DONE_WEEKLY_CLASSED);
					int competitionsWeeklyNonClassed = nobleInfo.getInteger(COMP_DONE_WEEKLY_NON_CLASSED);
					int compWon = nobleInfo.getInteger(COMP_WON);
					int compLost = nobleInfo.getInteger(COMP_LOST);
					int compDrawn = nobleInfo.getInteger(COMP_DRAWN);
					boolean toSave = nobleInfo.getBool("to_save");

					if(toSave)
					{
						statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES);
						statement.setInt(1, charId);
						statement.setInt(2, classId);
						statement.setInt(3, points);
						statement.setInt(4, compDone);
						statement.setInt(5, competitionsDoneWeekly);
						statement.setInt(6, competitionsDoneWeeklyClassed);
						statement.setInt(7, competitionsWeeklyNonClassed);
						statement.setInt(8, compWon);
						statement.setInt(9, compLost);
						statement.setInt(10, compDrawn);

						nobleInfo.set("to_save", false);
					}
					else
					{
						statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES);
						statement.setInt(1, points);
						statement.setInt(2, compDone);
						statement.setInt(3, competitionsDoneWeekly);
						statement.setInt(4, competitionsDoneWeeklyClassed);
						statement.setInt(5, competitionsWeeklyNonClassed);
						statement.setInt(6, compWon);
						statement.setInt(7, compLost);
						statement.setInt(8, compDrawn);
						statement.setInt(9, charId);
					}
					statement.execute();
				}
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "Olympiad System: Failed to save noblesse data to database: ", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	/**
	 * Save olympiad.properties file with current olympiad status and update noblesse table in database
	 */
	public void saveOlympiadStatus()
	{
		saveNobleData();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OLYMPIAD_SAVE_DATA);

			statement.setInt(1, _currentCycle);
			statement.setInt(2, _period);
			statement.setInt(3, _week);
			statement.setLong(4, _olympiadEnd);
			statement.setLong(5, _validationEnd);
			statement.setLong(6, _nextWeeklyChange);
			statement.setInt(7, _currentCycle);
			statement.setInt(8, _period);
			statement.setInt(9, _week);
			statement.setLong(10, _olympiadEnd);
			statement.setLong(11, _validationEnd);
			statement.setLong(12, _nextWeeklyChange);

			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Olympiad System: Failed to save olympiad data to database: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	protected void updateMonthlyData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(OLYMPIAD_MONTH_CLEAR);
			statement.execute();
			DatabaseUtils.closeStatement(statement);
			statement = con.prepareStatement(OLYMPIAD_MONTH_CREATE);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Olympiad System: Failed to update monthly noblese data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Resets number of matches, classed matches, non classed matches, team matches done by noble characters in the week.
	 */
	protected void resetWeeklyMatches()
	{
		if(_period == 1)
		{
			return;
		}

		for(StatsSet nobleInfo : _nobles.values())
		{
			nobleInfo.set(COMP_DONE_WEEKLY, 0);
			nobleInfo.set(COMP_DONE_WEEKLY_CLASSED, 0);
			nobleInfo.set(COMP_DONE_WEEKLY_NON_CLASSED, 0);
		}
		// Меняем неделю
		_week++;
		if(_week > 5)
		{
			_week = 1;
		}
	}

	protected void sortHerosToBe()
	{
		if(_period != 1)
		{
			return;
		}
		if(_nobles != null)
		{
			_logResults.log(Level.INFO, "Noble,charid,classid,compDone,points");
			StatsSet nobleInfo;
			for(Entry<Integer, StatsSet> entry : _nobles.entrySet())
			{
				nobleInfo = entry.getValue();

				if(nobleInfo == null)
				{
					continue;
				}

				int charId = entry.getKey();
				int classId = nobleInfo.getInteger(CLASS_ID);
				String charName = nobleInfo.getString(CHAR_NAME);
				int points = nobleInfo.getInteger(POINTS);
				int compDone = nobleInfo.getInteger(COMP_DONE);

				_logResults.log(Level.INFO, OlympiadLogFormatter.format(charName, new Object[]{
					charId, classId, compDone, points
				}));
			}
		}

		_heroesToBe = new L2FastList<>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OLYMPIAD_GET_HEROES);
			StatsSet hero;
			for(int HERO_ID : HERO_IDS)
			{
				statement.setInt(1, HERO_ID);
				rset = statement.executeQuery();
				statement.clearParameters();

				if(rset.next())
				{
					hero = new StatsSet();
					hero.set(CLASS_ID, HERO_ID);
					hero.set(CHAR_ID, rset.getInt(CHAR_ID));
					hero.set(CHAR_NAME, rset.getString(CHAR_NAME));

					_logResults.log(Level.INFO, OlympiadLogFormatter.format("Hero " + hero.getString(CHAR_NAME), new Object[]{
						hero.getInteger(CHAR_ID), hero.getInteger(CLASS_ID)
					}));
					_heroesToBe.add(hero);
				}
				DatabaseUtils.closeResultSet(rset);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Olympiad System: Couldnt load heros from DB");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public L2FastList<String> getClassLeaderBoard(int classId)
	{
		L2FastList<String> names = new L2FastList<>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_EACH_CLASS_LEADER);
			statement.setInt(1, classId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				names.add(rset.getString(CHAR_NAME));
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Olympiad System: Couldnt load olympiad leaders from DB");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return names;
	}

	public int getNoblessePasses(L2PcInstance player, boolean clear)
	{
		if(player == null || _period != 1 || _noblesRank.isEmpty())
		{
			return 0;
		}

		int objId = player.getObjectId();
		if(!_noblesRank.containsKey(objId))
		{
			return 0;
		}

		StatsSet noble = _nobles.get(objId);
		if(noble == null || noble.getInteger(POINTS) == 0)
		{
			return 0;
		}
		int rank = _noblesRank.get(objId);
		int points = player.getOlympiadController().isHero() ? Config.ALT_OLY_HERO_POINTS : 0;
		switch(rank)
		{
			case 1:
				points += Config.ALT_OLY_RANK1_POINTS;
				break;
			case 2:
				points += Config.ALT_OLY_RANK2_POINTS;
				break;
			case 3:
				points += Config.ALT_OLY_RANK3_POINTS;
				break;
			case 4:
				points += Config.ALT_OLY_RANK4_POINTS;
				break;
			default:
				points += Config.ALT_OLY_RANK5_POINTS;
		}

		if(clear)
		{
			noble.set(POINTS, 0);
			if(rank == 2 || rank == 3)
			{
				player.getVariablesController().set("olympiadRank2or3", true);
			}
		}

		points *= Config.ALT_OLY_GP_PER_POINT;

		return points;
	}

	public int getNoblePoints(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(POINTS);
	}

	public int getWeeklyBattles(int objId)
	{
		if(_nobles.isEmpty())
		{
			return 0;
		}

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
		{
			return 0;
		}

		return noble.getInteger(COMP_DONE_WEEKLY);
	}

	public int getOlympiadWeek()
	{
		return _week;
	}

	public int getLastNobleOlympiadPoints(int objId)
	{
		int result = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE charId = ?");
			statement.setInt(1, objId);
			rs = statement.executeQuery();
			if(rs.first())
			{
				result = rs.getInt(1);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not load last olympiad points:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return result;
	}

	public int getCompetitionDone(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE);
	}

	public int getCompetitionWon(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_WON);
	}

	public int getCompetitionLost(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_LOST);
	}

	/**
	 * Gets how many matches a noble character did in the week
	 * @param objId        id of a noble character
	 * @return number of weekly competitions done
	 * @see                #getRemainingWeeklyMatches(int)
	 */
	public int getCompetitionDoneWeek(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEKLY);
	}

	/**
	 * Gets how many classed matches a noble character did in the week
	 * @param objId        id of a noble character
	 * @return number of weekly <i>classed</i> competitions done
	 * @see                #getRemainingWeeklyMatchesClassed(int)
	 */
	public int getCompetitionDoneWeekClassed(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEKLY_CLASSED);
	}

	/**
	 * Gets how many non classed matches a noble character did in the week
	 * @param objId        id of a noble character
	 * @return number of weekly <i>non classed</i> competitions done
	 * @see                #getRemainingWeeklyMatchesNonClassed(int)
	 */
	public int getCompetitionDoneWeekNonClassed(int objId)
	{
		if(_nobles == null || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEKLY_NON_CLASSED);
	}

	/**
	 * Number of remaining matches a noble character can join in the week
	 * @param objId        id of a noble character
	 * @return difference between maximum allowed weekly matches and currently done weekly matches.
	 * @see                #getCompetitionDoneWeek(int)
	 * @see                Config#ALT_OLY_MAX_WEEKLY_MATCHES
	 */
	public int getRemainingWeeklyMatches(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES - getCompetitionDoneWeek(objId), 0);
	}

	/**
	 * Number of remaining <i>classed</i> matches a noble character can join in the week
	 * @param objId        id of a noble character
	 * @return difference between maximum allowed weekly classed matches and currently done weekly classed matches.
	 * @see                #getCompetitionDoneWeekClassed(int)
	 * @see                Config#ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED
	 */
	public int getRemainingWeeklyMatchesClassed(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED - getCompetitionDoneWeekClassed(objId), 0);
	}

	public int getParticipantCount()
	{
		return _nobles.size();
	}

	/**
	 * Number of remaining <i>non classed</i> matches a noble character can join in the week
	 * @param objId        id of a noble character
	 * @return difference between maximum allowed weekly non classed matches and currently done weekly non classed matches.
	 * @see                #getCompetitionDoneWeekNonClassed(int)
	 * @see                Config#ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED
	 */
	public int getRemainingWeeklyMatchesNonClassed(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED - getCompetitionDoneWeekNonClassed(objId), 0);
	}

	protected void deleteNobles()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OLYMPIAD_DELETE_ALL);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Olympiad System: Couldnt delete nobles from DB");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		_nobles.clear();
	}

	private static class SingletonHolder
	{
		protected static final Olympiad _instance = new Olympiad();
	}

	protected class OlympiadEndTask implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(_currentCycle));
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began.");

			if(_scheduledWeeklyTask != null)
			{
				_scheduledWeeklyTask.cancel(true);
			}

			saveNobleData();

			_period = 1;
			sortHerosToBe();
			HeroManager.getInstance().resetData();
			HeroManager.getInstance().computeNewHeroes(_heroesToBe);

			saveOlympiadStatus();
			updateMonthlyData();

			Calendar validationEnd = Calendar.getInstance();
			_validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;

			loadNoblesRank();
			_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
		}
	}

	protected class ValidationEndTask implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
			_period = 0;
			_currentCycle++;
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}
}
