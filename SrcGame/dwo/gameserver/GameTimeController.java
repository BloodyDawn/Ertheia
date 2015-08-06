package dwo.gameserver;

import dwo.config.Config;
import dwo.gameserver.instancemanager.DayNightSpawnManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Calendar;

public class GameTimeController extends Thread
{
	public static final int TICKS_PER_SECOND = 10; // not able to change this without checking through code
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	public static final int IG_DAYS_PER_DAY = 6;
	public static final int MILLIS_PER_IG_DAY = 3600000 * 24 / IG_DAYS_PER_DAY;
	public static final int SECONDS_PER_IG_DAY = MILLIS_PER_IG_DAY / 1000;
	public static final int MINUTES_PER_IG_DAY = SECONDS_PER_IG_DAY / 60;
	public static final int TICKS_PER_IG_DAY = SECONDS_PER_IG_DAY * TICKS_PER_SECOND;
	public static final int TICKS_SUN_STATE_CHANGE = TICKS_PER_IG_DAY / 4;
	protected static final Logger _log = LogManager.getLogger(GameTimeController.class);
	private static GameTimeController _instance;

	private final FastMap<Integer, L2Character> _movingObjects = new FastMap<Integer, L2Character>().shared();
	private final long _referenceTime;

	private GameTimeController()
	{
		super("GameTimeController");
		setDaemon(true);
		setPriority(MAX_PRIORITY);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		_referenceTime = c.getTimeInMillis();

		start();
	}

	public static void init()
	{
		_instance = new GameTimeController();
	}

	public static GameTimeController getInstance()
	{
		return _instance;
	}

	public int getGameTime()
	{
		return getGameTicks() % TICKS_PER_IG_DAY / MILLIS_IN_TICK;
	}

	public int getGameHour()
	{
		return getGameTime() / 60;
	}

	public int getGameMinute()
	{
		return getGameTime() % 60;
	}

	public boolean isNight()
	{
		return getGameHour() < 6;
	}

	/**
	 * @return true GameTime tick. Directly taken from current time. This represents the tick of the time.
	 */
	public int getGameTicks()
	{
		return (int) ((System.currentTimeMillis() - _referenceTime) / MILLIS_IN_TICK);
	}

	/**
	 * Add a L2Character to movingObjects of GameTimeController.
	 * @param cha The L2Character to add to movingObjects of GameTimeController
	 */
	public void registerMovingObject(L2Character cha)
	{
		if(cha == null)
		{
			return;
		}

		_movingObjects.putIfAbsent(cha.getObjectId(), cha);
	}

	/**
	 * Move all L2Characters contained in movingObjects of GameTimeController.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController.<BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <ul>
	 * <li>Update the position of each L2Character</li>
	 * <li>If movement is finished, the L2Character is removed from movingObjects</li>
	 * <li>Create a task to update the _knownObject and _knowPlayers of each L2Character that finished its movement and of their already known L2Object then notify AI with EVT_ARRIVED</li>
	 * </ul>
	 */
	private void moveObjects()
	{
		L2Character character;
		for(FastMap.Entry<Integer, L2Character> e = _movingObjects.head(), tail = _movingObjects.tail(); !(e = e.getNext()).equals(tail); )
		{
			character = e.getValue();

			if(character.updatePosition(getGameTicks()))
			{
				// Destination reached. Remove from map and execute arrive event.
				_movingObjects.remove(e.getKey());
				fireCharacterArrived(character);
			}
		}
	}

	private void fireCharacterArrived(final L2Character character)
	{
		final L2CharacterAI ai = character.getAI();
		if(ai == null)
		{
			return;
		}

		ThreadPoolManager.getInstance().executeAi(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(Config.MOVE_BASED_KNOWNLIST)
					{
						character.getKnownList().findObjects();
					}

					ai.notifyEvent(CtrlEvent.EVT_ARRIVED);
				}
				catch(Throwable e)
				{
					_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while running fireCharacterArrived()");
				}
			}
		});
	}

	public void stopTimer()
	{
		interrupt();
		_log.log(Level.INFO, getClass().getSimpleName() + ": Stopped.");
	}

	@Override
	public void run()
	{
		_log.log(Level.INFO, getClass().getSimpleName() + ": Started.");

		long nextTickTime;
		long sleepTime;
		boolean isNight = isNight();

		if(isNight)
		{
			ThreadPoolManager.getInstance().executeAi(() -> DayNightSpawnManager.getInstance().notifyChangeMode());
		}

		while(true)
		{
			nextTickTime = System.currentTimeMillis() / MILLIS_IN_TICK * MILLIS_IN_TICK + 100;

			try
			{
				moveObjects();
			}
			catch(Throwable e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while running main thread.");
			}

			sleepTime = nextTickTime - System.currentTimeMillis();
			if(sleepTime > 0)
			{
				try
				{
					Thread.sleep(sleepTime);
				}
				catch(InterruptedException e)
				{

				}
			}

			if(isNight() != isNight)
			{
				isNight = !isNight;

				ThreadPoolManager.getInstance().executeAi(() -> DayNightSpawnManager.getInstance().notifyChangeMode());
			}
		}
	}
}