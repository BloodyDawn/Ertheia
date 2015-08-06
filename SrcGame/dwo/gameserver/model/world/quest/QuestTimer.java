package dwo.gameserver.model.world.quest;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;

public class QuestTimer
{
	protected static final Logger _log = LogManager.getLogger(QuestTimer.class);
	private boolean _isActive = true;
	private String _name;
	private Quest _quest;
	private L2Npc _npc;
	private L2PcInstance _player;
	private boolean _isRepeating;
	private ScheduledFuture<?> _schedular;

	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		_name = name;
		_quest = quest;
		_player = player;
		_npc = npc;
		_isRepeating = repeating;
		_schedular = repeating ? ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time) : ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time);
	}

	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player)
	{
		this(quest, name, time, npc, player, false);
	}

	public QuestTimer(QuestState qs, String name, long time)
	{
		this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
	}

	/**
	 * Cancel this quest timer.
	 */
	public void cancel()
	{
		_isActive = false;
		if(_schedular != null)
		{
			_schedular.cancel(false);
		}
	}

	/**
	 * Cancel this quest timer and remove it from the associated quest.
	 */
	public void cancelAndRemove()
	{
		cancel();
		_quest.removeQuestTimer(this);
	}

	/**
	 * public method to compare if this timer matches with the key attributes passed.
	 * @param quest : Quest instance to which the timer is attached
	 * @param name : Name of the timer
	 * @param npc : Npc instance attached to the desired timer (null if no npc attached)
	 * @param player : Player instance attached to the desired timer (null if no player attached)
	 * @return
	 */
	public boolean isMatch(Quest quest, String name, L2Npc npc, L2PcInstance player)
	{
		if(quest == null || name == null)
		{
			return false;
		}
		if(quest != _quest || !name.equalsIgnoreCase(_name))
		{
			return false;
		}
		return npc == _npc && player == _player;
	}

	public boolean getIsActive()
	{
		return _isActive;
	}

	public boolean getIsRepeating()
	{
		return _isRepeating;
	}

	public Quest getQuest()
	{
		return _quest;
	}

	public String getName()
	{
		return _name;
	}

	public L2Npc getNpc()
	{
		return _npc;
	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	@Override
	public String toString()
	{
		return _name;
	}

	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!getIsActive())
			{
				return;
			}

			try
			{
				if(!getIsRepeating())
				{
					cancelAndRemove();
				}
				getQuest().notifyEvent(getName(), getNpc(), getPlayer());
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error in QuestTimer: Event name: " + _name + " QuestName: " + _quest.getName(), e);
			}
		}
	}
}
