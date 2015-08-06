package dwo.gameserver.instancemanager.events.TvT;

import dwo.config.events.ConfigEventTvT;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

public class TvTManager
{
	protected static final Logger _log = LogManager.getLogger(TvTManager.class);

	/** Task for event cycles<br> */
	private TvTStartTask _task;

	private boolean _inited;

	/**
	 * New instance only by getInstance()<br>
	 */
	private TvTManager()
	{
		checkAndInit();
	}

	/**
	 * Initialize new/Returns the one and only instance<br><br>
	 *
	 * @return TvTManager<br>
	 */
	public static TvTManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void checkAndInit()
	{
		if(ConfigEventTvT.TVT_EVENT_ENABLED && !_inited)
		{
			TvTEvent.init();

			scheduleEventStart();
			_inited = true;
			_log.log(Level.INFO, "TvTEventEngine[TvTManager.TvTManager()]: STARTED.");
		}
		else
		{
			_log.log(Level.INFO, "TvTEventEngine[TvTManager.TvTManager()]: Engine is disabled.");
		}
	}

	/**
	 * Starts TvTStartTask
	 */
	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for(String timeOfDay : ConfigEventTvT.TVT_EVENT_INTERVAL)
			{
				// Creating a Calendar object from the specified interval value
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
				if(testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				// Check for the test date to be the minimum (smallest in the specified list)
				if(nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
				{
					nextStartTime = testStartTime;
				}
			}
			_task = new TvTStartTask(nextStartTime.getTimeInMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "TvTEventEngine[TvTManager.scheduleEventStart()]: Error figuring out a start time. Check TvTEventInterval in Config file.");
		}
	}

	public long getNextStartTime()
	{
		if(_task == null)
		{
			return 0;
		}

		return _task._startTime;
	}

	/**
	 * Method to start participation
	 */
	public void startReg()
	{
		if(TvTEvent.startParticipation())
		{
			//Announcements.getInstance().announceToAll("Стенка на стенку: Регистрация будет доступна через " + Config.TVT_EVENT_PARTICIPATION_TIME+ " минут(ы).");
			// schedule registration end
			_task.setStartTime(System.currentTimeMillis() + 60000L * ConfigEventTvT.TVT_EVENT_PARTICIPATION_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			Announcements.getInstance().announceToAll("Стенка на стенку: Ивент отменен.");
			_log.log(Level.ERROR, "TvTEventEngine[TvTManager.run()]: Error spawning event npc for participation.");

			scheduleEventStart();
		}
	}

	/**
	 * Method to start the fight
	 */
	public void startEvent()
	{
		if(TvTEvent.startFight())
		{
			TvTEvent.sysMsgToAllParticipants("Стенка на стенку: Телепортация участников на Арену через " + ConfigEventTvT.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " секунд(ы).");
			_task.setStartTime(System.currentTimeMillis() + 60000L * ConfigEventTvT.TVT_EVENT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			Announcements.getInstance().announceToAll("Стенка на стенку: Ивент отменен, так как нет желающих.");
			_log.info("TvTEventEngine[TvTManager.run()]: Lack of registration, abort event.");

			scheduleEventStart();
		}
	}

	public void startOnce()
	{
		if(!_inited)
		{
			TvTEvent.init();
			_inited = true;
		}

		if(_task == null)
		{
			_task = new TvTStartTask(System.currentTimeMillis());
		}
		else
		{
			_task.setStartTime(System.currentTimeMillis());
		}

		_log.log(Level.INFO, "TvTEventEngine[TvTManager.TvTManager()]: STARTED.");
		ThreadPoolManager.getInstance().executeTask(_task);
	}

	public void stopEvent()
	{
		if(_task != null)
		{
			if(!_inited)
			{
				TvTEvent.init();
				_inited = true;
			}

			_task.nextRun.cancel(true);
			Announcements.getInstance().announceToAll("Стенка на стенку: Ивент отменен.");
		}
	}

	/**
	 * Method to end the event and reward
	 */
	public void endEvent()
	{
		Announcements.getInstance().announceToAll(TvTEvent.calculateRewards());
		TvTEvent.sysMsgToAllParticipants("Стенка на стенку: Телепортация обратно к меннеджеру через " + ConfigEventTvT.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " секунд(ы).");
		TvTEvent.stopFight();

		scheduleEventStart();
	}

	public void skipDelay()
	{
		if(_task.nextRun.cancel(false))
		{
			_task.setStartTime(System.currentTimeMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}

	public boolean isInitted()
	{
		return _inited;
	}

	private static class SingletonHolder
	{
		protected static final TvTManager _instance = new TvTManager();
	}

	/**
	 * Class for TvT cycles
	 */
	class TvTStartTask implements Runnable
	{
		public ScheduledFuture<?> nextRun;
		private long _startTime;

		public TvTStartTask(long startTime)
		{
			_startTime = startTime;
		}

		public void setStartTime(long startTime)
		{
			_startTime = startTime;
		}

		@Override
		public void run()
		{
			int delay = (int) Math.round((_startTime - System.currentTimeMillis()) / 1000.0);

			int nextMsg = 0;
			if(delay > 3600)              // 60 минут
			{
				announce(delay);
				nextMsg = delay - 3600;
			}
			else if(delay > 1800)          // 30 минут
			{
				announce(delay);
				nextMsg = delay - 1800;
			}
			else if(delay > 900)          // 15 минут
			{
				announce(delay);
				nextMsg = delay - 900;
			}
			else if(delay > 600)          // 10 минут
			{
				announce(delay);
				nextMsg = delay - 600;
			}
			else if(delay > 300)          // 5 минут
			{
				announce(delay);
				nextMsg = delay - 300;
			}
			else if(delay > 180)          // 3 минуты
			{
				announce(delay);
				nextMsg = delay - 180;
			}
			else if(delay > 120)          // 2 минуты
			{
				announce(delay);
				nextMsg = delay - 120;
			}
			else if(delay > 60)           // 1 минута
			{
				announce(delay);
				nextMsg = delay - 60;
			}
			else if(delay > 0)
			{
				nextMsg = delay;
			}
			else
			{
				// start
				if(TvTEvent.isInactive())
				{
					startReg();
				}
				else if(TvTEvent.isParticipating())
				{
					startEvent();
				}
				else
				{
					endEvent();
				}
			}

			if(delay > 0)
			{
				nextRun = ThreadPoolManager.getInstance().scheduleGeneral(this, nextMsg * 1000);
			}
		}

		private void announce(long time)
		{
			if(time >= 3600 && time % 3600 == 0)
			{
				if(TvTEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Стенка на стенку: Регистрация в Адене будет закрыта через " + time / 60 / 60 + " час(а)!");
				}
				else if(TvTEvent.isStarted())
				{
					TvTEvent.sysMsgToAllParticipants("Стенка на стенку: Ивент закончится через " + time / 60 / 60 + " час(а)!");
				}
			}
			else if(time >= 60)
			{
				if(TvTEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Стенка на стенку: Регистрация в Адене будет закрыта через " + time / 60 + " минут(ы)!");
				}
				else if(TvTEvent.isStarted())
				{
					TvTEvent.sysMsgToAllParticipants("Стенка на стенку: Ивент закончится через " + time / 60 + " минут(ы)!");
				}
			}
			else
			{
				if(TvTEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Стенка на стенку: Регистрация в Адене будет закрыта через " + time + " секунд(ы)!");
				}
				else if(TvTEvent.isStarted())
				{
					TvTEvent.sysMsgToAllParticipants("Стенка на стенку: Ивент закончится через " + time + " секунд(ы)!");
				}
			}
		}
	}
}