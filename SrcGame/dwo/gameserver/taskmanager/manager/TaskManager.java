package dwo.gameserver.taskmanager.manager;

import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.tasks.*;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static dwo.gameserver.taskmanager.TaskTypes.TYPE_NONE;
import static dwo.gameserver.taskmanager.TaskTypes.TYPE_SHEDULED;
import static dwo.gameserver.taskmanager.TaskTypes.TYPE_TIME;
import static dwo.gameserver.taskmanager.TaskTypes.valueOf;

/**
 * @author Layane
 */

public class TaskManager
{
	protected static final Logger _log = LogManager.getLogger(TaskManager.class);

	protected static final String[] SQL_STATEMENTS = {
		"SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks",
		"UPDATE global_tasks SET last_activation=? WHERE id=?", "SELECT id FROM global_tasks WHERE task=?",
		"INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)"
	};
	final List<ExecutedTask> _currentTasks = new ArrayList<>();
	private final Map<Integer, Task> _tasks = new ConcurrentHashMap<>();

	private TaskManager()
	{
		initialization();
		startAllTasks();
	}

	public static TaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addUniqueTask(task, type, param1, param2, param3, 0);
	}

	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_STATEMENTS[2]);
			statement.setString(1, task);
			rset = statement.executeQuery();

			if(!rset.next())
			{
				statement = con.prepareStatement(SQL_STATEMENTS[3]);
				statement.setString(1, task);
				statement.setString(2, type.toString());
				statement.setLong(3, lastActivation);
				statement.setString(4, param1);
				statement.setString(5, param2);
				statement.setString(6, param3);
				statement.execute();
			}
			return true;
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Cannot add the unique task: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		return false;
	}

	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addTask(task, type, param1, param2, param3, 0);
	}

	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_STATEMENTS[3]);
			statement.setString(1, task);
			statement.setString(2, type.toString());
			statement.setLong(3, lastActivation);
			statement.setString(4, param1);
			statement.setString(5, param2);
			statement.setString(6, param3);
			statement.execute();
			return true;
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Cannot add the task:  " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	private void initialization()
	{
		registerTask(new TaskAccountDataSave());
		registerTask(new TaskBirthday());
		registerTask(new TaskDailySkillReuseClean());
		registerTask(new TaskGlobalVariablesSave());
		registerTask(new TaskOlympiadSave());
		registerTask(new TaskRaidPointsReset());
		registerTask(new TaskRecommendationReset());
		registerTask(new TaskRestart());
		registerTask(new TaskShutdown());
		registerTask(new TaskVitalityReset());
		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			registerTask(new TaskWorldStatisticsSave());
			registerTask(new TaskWorldStatisticsResultUpdate());
		}
		registerTask(new TaskChaosFestivalRound());
		registerTask(new TaskResetPlayerDailyVariables());
		if(ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_SYSTEM_ENABLE)
		{
			registerTask(new TaskRssReload());
		}
        registerTask(new TaskDailyWorldChatPointReset());
	}

	public void registerTask(Task task)
	{
		int key = task.getName().hashCode();
		if(!_tasks.containsKey(key))
		{
			_tasks.put(key, task);
			task.initializate();
		}
	}

	private void startAllTasks()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_STATEMENTS[0]);
			rset = statement.executeQuery();

			while(rset.next())
			{
				Task task = _tasks.get(rset.getString("task").trim().toLowerCase().hashCode());

				if(task == null)
				{
					continue;
				}

				TaskTypes type = valueOf(rset.getString("type"));

				if(type != TYPE_NONE)
				{
					ExecutedTask current = new ExecutedTask(task, type, rset);
					if(launchTask(current))
					{
						_currentTasks.add(current);
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while loading Global Task table: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private boolean launchTask(ExecutedTask task)
	{
		ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
		TaskTypes type = task.getType();
		long delay;
		long interval;

		switch(type)
		{
			case TYPE_STARTUP:
				task.run();
				return false;
			case TYPE_SHEDULED:
				delay = Long.valueOf(task.getParams()[0]);
				task.scheduled = scheduler.scheduleGeneral(task, delay);
				return true;
			case TYPE_FIXED_SHEDULED:
				delay = Long.valueOf(task.getParams()[0]);
				interval = Long.valueOf(task.getParams()[1]);
				task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
				return true;
			case TYPE_TIME:
				try
				{
					Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
					long diff = desired.getTime() - System.currentTimeMillis();
					if(diff >= 0)
					{
						task.scheduled = scheduler.scheduleGeneral(task, diff);
						return true;
					}
					_log.log(Level.INFO, "Task " + task.getId() + " is obsoleted.");
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Error in case TYPE_TIME: " + task.getId() + ": " + e.getMessage(), e);
				}
				break;
			case TYPE_SPECIAL:
				ScheduledFuture<?> result = task.getTask().launchSpecial(task);
				if(result != null)
				{
					task.scheduled = result;
					return true;
				}
				break;
			case TYPE_GLOBAL_TASK:
				interval = Long.valueOf(task.getParams()[0]) * 86400000L;
				String[] hour = task.getParams()[1].split(":");

				if(hour.length != 3)
				{
					_log.log(Level.WARN, "Task " + task.getId() + " has incorrect parameters");
					return false;
				}

				Calendar check = Calendar.getInstance();
				check.setTimeInMillis(task.getLastActivation() + interval);

				Calendar min = Calendar.getInstance();
				try
				{
					min.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
					min.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
					min.set(Calendar.SECOND, Integer.parseInt(hour[2]));
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Bad parameter on task " + task.getId() + ": " + e.getMessage(), e);
					return false;
				}

				delay = min.getTimeInMillis() - System.currentTimeMillis();

				if(check.after(min) || delay < 0)
				{
					delay += interval;
				}

				task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);

				return true;

			default:
				return false;
		}

		return false;
	}

	private static class SingletonHolder
	{
		protected static final TaskManager _instance = new TaskManager();
	}

	public class ExecutedTask implements Runnable
	{
		final int id;
		long lastActivation;
		Task task;
		TaskTypes type;
		String[] params;
		ScheduledFuture<?> scheduled;

		public ExecutedTask(Task ptask, TaskTypes ptype, ResultSet rset) throws SQLException
		{
			task = ptask;
			type = ptype;
			id = rset.getInt("id");
			lastActivation = rset.getLong("last_activation");
			params = new String[]{rset.getString("param1"), rset.getString("param2"), rset.getString("param3")};
		}

		@Override
		public void run()
		{
			task.onTimeElapsed(this);
			lastActivation = System.currentTimeMillis();

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(SQL_STATEMENTS[1]);
				statement.setLong(1, lastActivation);
				statement.setInt(2, id);
				statement.executeUpdate();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "Cannot updated the Global Task " + id + ": " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}

			if(type == TYPE_SHEDULED || type == TYPE_TIME)
			{
				stopTask();
			}
		}

		@Override
		public int hashCode()
		{
			return id;
		}

		@Override
		public boolean equals(Object object)
		{
			if(this == object)
			{
				return true;
			}
			if(!(object instanceof ExecutedTask))
			{
				return false;
			}
			return id == ((ExecutedTask) object).id;
		}

		public Task getTask()
		{
			return task;
		}

		public TaskTypes getType()
		{
			return type;
		}

		public int getId()
		{
			return id;
		}

		public String[] getParams()
		{
			return params;
		}

		public long getLastActivation()
		{
			return lastActivation;
		}

		public void stopTask()
		{
			task.onDestroy();

			if(scheduled != null)
			{
				scheduled.cancel(true);
			}

			_currentTasks.remove(this);
		}
	}
}