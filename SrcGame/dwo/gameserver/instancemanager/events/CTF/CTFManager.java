package dwo.gameserver.instancemanager.events.CTF;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

public class CTFManager
{
	protected static final Logger _log = LogManager.getLogger(CTFManager.class);
	private CTFStartTask _task;

	private CTFManager()
	{
		if(ConfigEventCTF.CTF_EVENT_ENABLED)
		{
			CTFEvent.init();

			scheduleCTFStart();
			_log.log(Level.INFO, "CTFEventEngine[CTFManager.CTFManager()]: STARTED.");
		}
		else
		{
			_log.log(Level.INFO, "CTFEventEngine[CTFManager.CTFManager()]: Engine is disabled.");
		}
	}

	public static CTFManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void scheduleCTFStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for(String timeOfDay : ConfigEventCTF.CTF_EVENT_INTERVAL)
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
			_task = new CTFStartTask(nextStartTime.getTimeInMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CTFEventEngine[CTFManager.scheduleEventStart()]: Error figuring out a start time. Check CTFEventInterval in config file.");
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
		if(CTFEvent.startParticipation())
		{
			Announcements.getInstance().announceToAll("Захват флага: Регистрация доступна " + ConfigEventCTF.CTF_EVENT_PARTICIPATION_TIME + " минут(ы).");

			// schedule registration end
			_task.setStartTime(System.currentTimeMillis() + 60000L * ConfigEventCTF.CTF_EVENT_PARTICIPATION_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			Announcements.getInstance().announceToAll("Захват флага: Ивент отменен.");
			_log.log(Level.WARN, "CTFEventEngine[CTFManager.run()]: Error spawning event npc for participation.");

			scheduleCTFStart();
		}
	}

	public void startEvent()
	{
		if(CTFEvent.startFight())
		{
			CTFEvent.sysMsgToAllParticipants("Захват флага: Телепортация участников на Арену через " + ConfigEventCTF.CTF_EVENT_START_LEAVE_TELEPORT_DELAY + " секунд(ы).");
			_task.setStartTime(System.currentTimeMillis() + 60000L * ConfigEventCTF.CTF_EVENT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			Announcements.getInstance().announceToAll("Захват флага: Ивент отменен, так как нет желающих.");
			_log.info("CTFEventEngine[CTFManager.run()]: Lack of registration, abort event.");
			scheduleCTFStart();
		}
	}

	public void endEvent()
	{
		Announcements.getInstance().announceToAll(CTFEvent.calculateWinner());
		CTFEvent.sysMsgToAllParticipants("Захват флага: Телепортация обратно к меннеджеру через " + ConfigEventCTF.CTF_EVENT_START_LEAVE_TELEPORT_DELAY + " секунд(ы).");
		CTFEvent.stopFight();

		scheduleCTFStart();
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
		protected static final CTFManager _instance = new CTFManager();
	}

	class CTFStartTask implements Runnable
	{
		public ScheduledFuture<?> nextRun;
		private long _startTime;

		public CTFStartTask(long startTime)
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
				CTF_Announcements(delay);
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
				if(CTFEvent.isInactive())
				{
					startReg();
				}
				else if(CTFEvent.isParticipating())
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

		private void CTF_Announcements(long time)
		{
			if(time >= 3600 && time % 3600 == 0)
			{
				if(CTFEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Захват флага: Регистрация будет закрыта через " + time / 60 / 60 + " час(а)!");
				}
				else if(CTFEvent.isStarted())
				{
					CTFEvent.sysMsgToAllParticipants("Захват флага: Ивент закончится через " + time / 60 / 60 + " час(а)!");
				}
			}
			else if(time >= 60)
			{
				if(CTFEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Захват флага: Регистрация будет закрыта через " + time / 60 + " минут(ы)!");
				}
				else if(CTFEvent.isStarted())
				{
					CTFEvent.sysMsgToAllParticipants("Захват флага: Ивент закончится через " + time / 60 + " минут(ы)!");
				}
			}
			else
			{
				if(CTFEvent.isParticipating())
				{
					Announcements.getInstance().announceToAll("Захват флага: Регистрация будет закрыта через " + time + " секунд(ы)!");
				}
				else if(CTFEvent.isStarted())
				{
					CTFEvent.sysMsgToAllParticipants("Захват флага: Ивент закончится через " + time + " секунд(ы)!");
				}
			}
		}
	}
}