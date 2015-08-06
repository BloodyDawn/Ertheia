package dwo.gameserver.instancemanager.events.KOTH;

import dwo.config.events.ConfigEventKOTH;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Nik
 * Some of the code is taken and modified from FBIagent's TvT Event,
 * to make this event more comfortable for the users when they set
 * the configuration for the event, and for the players when they
 * participate in it.
 */
public class KOTHManager
{
	protected static final Logger _log = LogManager.getLogger(KOTHManager.class.getName());
	private KOTHStartTask _task;

	private KOTHManager()
	{
		if(ConfigEventKOTH.KOTH_EVENT_ENABLED)
		{
			KOTHEvent.init();

			scheduleKOTHStart();
			_log.log(Level.INFO, "KOTHEventEngine[KOTHManager.KOTHManager()]: STARTED.");
		}
		else
		{
			_log.log(Level.INFO, "KOTHEventEngine[KOTHManager.KOTHManager()]: Engine is disabled.");
		}
	}

	public static KOTHManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void scheduleKOTHStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for(String timeOfDay : ConfigEventKOTH.KOTH_EVENT_INTERVAL)
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
				if(nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
				{
					nextStartTime = testStartTime;
				}
			}
			_task = new KOTHStartTask(nextStartTime.getTimeInMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "KOTHEventEngine[KOTHManager.scheduleEventStart()]: Error figuring out a start time. Check KOTHEventInterval in config file.");
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

	public void startReg()
	{
		if(KOTHEvent.startParticipation())
		{
			Announcements.getInstance().announceToAll("Царь горы: Регистрация будет доступна через " + ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_TIME + " минут(ы).");

			// schedule registration end
			_task.setStartTime(System.currentTimeMillis() + 60000L * ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			Announcements.getInstance().announceToAll("Царь горы: Ивент отменен.");
			_log.log(Level.WARN, "KOTHEventEngine[KOTHManager.run()]: Error spawning event npc for participation.");

			scheduleKOTHStart();
		}
	}

	public void startEvent()
	{
		if(KOTHEvent.startFight())
		{
			KOTHEvent.sysMsgToAllParticipants("Царь горы: Телепортация участников на Арену через " + ConfigEventKOTH.KOTH_EVENT_START_LEAVE_TELEPORT_DELAY + " секунд(ы).");
			_task.setStartTime(System.currentTimeMillis() + 60000L * ConfigEventKOTH.KOTH_EVENT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			Announcements.getInstance().announceToAll("Царь горы: Ивент отменен, так как нет желающих.");
			_log.info("KOTHEventEngine[KOTHManager.run()]: Lack of registration, abort event.");

			scheduleKOTHStart();
		}
	}

	public void endEvent()
	{
		Announcements.getInstance().announceToAll(KOTHEvent.calculateWinner());
		KOTHEvent.sysMsgToAllParticipants("Царь горы: Телепортация обратно к меннеджеру через " + ConfigEventKOTH.KOTH_EVENT_START_LEAVE_TELEPORT_DELAY + " секунд(ы).");
		KOTHEvent.stopFight();

		scheduleKOTHStart();
	}

	public void skipDelay()
	{
		if(_task.nextRun.cancel(false))
		{
			_task.setStartTime(System.currentTimeMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}

	private static class SingletonHolder
	{
		protected static final KOTHManager _instance = new KOTHManager();
	}

	class KOTHStartTask implements Runnable
	{
		public ScheduledFuture<?> nextRun;
		private long _startTime;

		public KOTHStartTask(long startTime)
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

			if(delay > 0)
			{
				KOTH_Announcements(delay);
			}

			int nextMsg = 0;
			if(delay > 3600)
			{
				nextMsg = delay - 3600;
			}
			else if(delay > 1800)
			{
				nextMsg = delay - 1800;
			}
			else if(delay > 900)
			{
				nextMsg = delay - 900;
			}
			else if(delay > 600)
			{
				nextMsg = delay - 600;
			}
			else if(delay > 300)
			{
				nextMsg = delay - 300;
			}
			else if(delay > 60)
			{
				nextMsg = delay - 60;
			}
			else if(delay > 5)
			{
				nextMsg = delay - 5;
			}
			else if(delay > 0)
			{
				nextMsg = delay;
			}
			else
			{
				// start
				if(KOTHEvent.isInactive())
				{
					startReg();
				}
				else if(KOTHEvent.isParticipating())
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

		private void KOTH_Announcements(long time)
		{
			if(time >= 3600 && time % 3600 == 0)
			{
				if(KOTHEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Царь горы: Регистрация будет закрыта через " + time / 60 / 60 + " час(а)!");
				}
				else if(KOTHEvent.isStarted())
				{
					KOTHEvent.sysMsgToAllParticipants("Царь горы: Ивент закончится через " + time / 60 / 60 + " час(а)!");
				}
			}
			else if(time >= 60)
			{
				if(KOTHEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Царь горы: Регистрация будет закрыта через " + time / 60 + " минут(ы)!");
				}
				else if(KOTHEvent.isStarted())
				{
					KOTHEvent.sysMsgToAllParticipants("Царь горы: Ивент закончится через " + time / 60 + " минут(ы)!");
				}
			}
			else
			{
				if(KOTHEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Царь горы: Регистрация будет закрыта через " + time + " секунд(ы)!");
				}
				else if(KOTHEvent.isStarted())
				{
					KOTHEvent.sysMsgToAllParticipants("Царь горы: Ивент закончится через " + time + " секунд(ы)!");
				}
			}
		}
	}
}