package dwo.gameserver.model.actor.knownlist;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

public class NpcKnownList extends CharKnownList
{
	private ScheduledFuture<?> _trackingTask;

	private int _distanceToWatch = 500;

	public NpcKnownList(L2Npc activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if(!(object instanceof L2Character))
		{
			return 0;
		}

		if(object.isPlayable())
		{
			return 1500;
		}

		return _distanceToWatch;
	}

	public void setDistanceToWatch(int distance)
	{
		_distanceToWatch = distance;
	}

	//L2Master mod - support for Walking monsters aggro
	public void startTrackingTask()
	{
		if(_trackingTask == null && getActiveChar().getAggroRange() > 0)
		{
			_trackingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new TrackingTask(), 2000, 2000);
		}
	}

	//L2Master mod - support for Walking monsters aggro
	public void stopTrackingTask()
	{
		if(_trackingTask != null)
		{
			_trackingTask.cancel(true);
			_trackingTask = null;
		}
	}

	//L2Master mod - support for Walking monsters aggro
	private class TrackingTask implements Runnable
	{
		public TrackingTask()
		{
			//
		}

		@Override
		public void run()
		{
			if(getActiveChar() instanceof L2Attackable)
			{
				L2Attackable monster = (L2Attackable) getActiveChar();
				if(monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
				{
					Collection<L2PcInstance> players = getKnownPlayers().values();
					if(!players.isEmpty())
					{
						for(L2PcInstance pl : players)
						{
							if(pl != null && pl.isInsideRadius(monster, monster.getAggroRange(), true, false) && !pl.isDead() && !pl.isInvul())
							{
								WalkingManager.getInstance().stopMoving(getActiveChar(), false);
								monster.addDamageHate(pl, 0, 100);
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pl, null);
								break;
							}
						}
					}
				}
			}
		}
	}
}
