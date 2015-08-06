package dwo.gameserver.taskmanager.manager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author la2 Lets drink to code!
 */

public class DecayTaskManager
{
	public static final int PET_DECAY_DELAY = 86400000; // 24 hours
	protected static final Logger _log = LogManager.getLogger(DecayTaskManager.class);
	protected final Map<L2Character, Long> _decayTasks = new FastMap<L2Character, Long>().shared();

	private DecayTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, 3000);
	}

	public static DecayTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addDecayTask(L2Character actor)
	{
		addDecayTask(actor, 0);
	}

	public void addDecayTask(L2Character actor, int interval)
	{
		_decayTasks.put(actor, System.currentTimeMillis() + interval);
	}

	public void cancelDecayTask(L2Character actor)
	{
		_decayTasks.remove(actor);
	}

	@Override
	public String toString()
	{
		String ret = "============= DecayTask Manager Report ============\r\n";
		ret += "Tasks count: " + _decayTasks.size() + "\r\n";
		ret += "Tasks dump:\r\n";

		Long current = System.currentTimeMillis();
		for(Entry<L2Character, Long> l2CharacterLongEntry : _decayTasks.entrySet())
		{
			ret += "Class/Name: " + l2CharacterLongEntry.getKey().getClass().getSimpleName() + '/' + l2CharacterLongEntry.getKey().getName() + " decay timer: " + (current - l2CharacterLongEntry.getValue()) + "\r\n";
		}

		return ret;
	}

	/**
	 * <u><b><font color="FF0000">Read only</font></b></u>
	 */
	public Map<L2Character, Long> getTasks()
	{
		return _decayTasks;
	}

	private static class SingletonHolder
	{
		protected static final DecayTaskManager _instance = new DecayTaskManager();
	}

	private class DecayScheduler implements Runnable
	{
		protected DecayScheduler()
		{
			// Do nothing
		}

		@Override
		public void run()
		{
			long current = System.currentTimeMillis();
			try
			{
				Iterator<Entry<L2Character, Long>> it = _decayTasks.entrySet().iterator();
				Entry<L2Character, Long> e;
				L2Character actor;
				Long next;
				int delay;
				while(it.hasNext())
				{
					e = it.next();
					actor = e.getKey();
					next = e.getValue();
					if(actor == null || next == null)
					{
						continue;
					}
					if(actor.isRaid() && !actor.isRaidMinion())
					{
						delay = Config.RAID_BOSS_DECAY_TIME;
					}
					else if(actor instanceof L2Attackable && (((L2Attackable) actor).isSpoil() || ((L2Attackable) actor).isSeeded()))
					{
						delay = Config.SPOILED_DECAY_TIME;
					}
					else if(actor.isPet())
					{
						delay = PET_DECAY_DELAY;
					}
					else
					{
						delay = actor.isSummon() ? 20000 : Config.NPC_DECAY_TIME;
					}
					if(current - next > delay)
					{
						actor.getLocationController().delete();
						it.remove();
					}
				}
			}
			catch(Exception e)
			{
				// TODO: Find out the reason for exception. Unless caught here, mob decay would stop.
				_log.log(Level.ERROR, "Error in DecayScheduler: " + e.getMessage(), e);
			}
		}
	}
}
