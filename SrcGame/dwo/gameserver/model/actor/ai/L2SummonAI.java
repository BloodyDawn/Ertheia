package dwo.gameserver.model.actor.ai;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Character.AIAccessor;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

import java.util.concurrent.Future;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_IDLE;

public class L2SummonAI extends L2PlayableAI implements Runnable
{
	private static final int AVOID_RADIUS = 70;

	private volatile boolean _thinking; // to prevent recursive thinking
	private volatile boolean _startFollow = ((L2Summon) _actor).getFollowStatus();
	private volatile boolean _attackMode = ((L2Summon) _actor).isDefendingMode();
	private L2Character _lastAttack;

	private volatile boolean _startAvoid;
	private Future<?> _avoidTask;

	public L2SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		_startFollow = false;
		onIntentionActive();
	}

	@Override
	protected void onIntentionActive()
	{
		L2Summon summon = (L2Summon) _actor;
		if(_startFollow)
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}

	@Override
	protected void onEvtThink()
	{
		if(_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		_thinking = true;

		// При любом случае, если владелец ушел на определенное расстояние, то возвращаем саммонов к нему
		if(getIntention() != AI_INTENTION_FOLLOW)
		{
			if(!Util.checkIfInRange(2000, getActor(), ((L2Summon) getActor()).getOwner(), true))
			{
				setIntention(AI_INTENTION_FOLLOW, ((L2Summon) getActor()).getOwner());
			}
		}

		try
		{
			switch(getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		super.onEvtAttacked(attacker);

		avoidAttack(attacker);
	}

	@Override
	protected void onEvtEvaded(L2Character attacker)
	{
		super.onEvtEvaded(attacker);

		avoidAttack(attacker);
	}

	@Override
	protected void onEvtFinishCasting()
	{
		if(_lastAttack == null)
		{
			((L2Summon) _actor).setFollowStatus(_startFollow);
		}
		else
		{
			setIntention(AI_INTENTION_ATTACK, _lastAttack);
			_lastAttack = null;
		}
	}

	@Override
	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		synchronized(this)
		{
			switch(intention)
			{
				case AI_INTENTION_ACTIVE:
				case AI_INTENTION_FOLLOW:
					startAvoidTask();
					break;
				default:
					stopAvoidTask();
			}

			super.changeIntention(intention, arg0, arg1);
		}
	}

	@Override
	public void stopAITask()
	{
		stopAvoidTask();
		super.stopAITask();
	}

	private void thinkAttack()
	{
		if(checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}
		if(maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
		{
			return;
		}
		clientStopMoving(null);
		_accessor.doAttack(getAttackTarget());
	}

	private void thinkCast()
	{
		L2Summon summon = (L2Summon) _actor;
		if(checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		boolean val = _startFollow;
		if(maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		clientStopMoving(null);
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		_startFollow = val;
		_accessor.doCast(_skill);
	}

	private void thinkPickUp()
	{
		if(checkTargetLost(getTarget()))
		{
			return;
		}
		if(maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		setIntention(AI_INTENTION_IDLE);
		((L2Summon.AIAccessor) _accessor).doPickupItem(getTarget());
	}

	private void thinkInteract()
	{
		if(checkTargetLost(getTarget()))
		{
			return;
		}
		if(maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		setIntention(AI_INTENTION_IDLE);
	}

	private void avoidAttack(L2Character attacker)
	{
		// trying to avoid if summon near owner
		if(((L2Summon) _actor).getOwner() != null && !((L2Summon) _actor).getOwner().equals(attacker) && ((L2Summon) _actor).getOwner().isInsideRadius(_actor, 2 * AVOID_RADIUS, true, false))
		{
			_startAvoid = true;
		}
	}

	@Override
	public void run()
	{
		if(_startAvoid)
		{
			_startAvoid = false;
			if(!_clientMoving && !_actor.isDead() && !_actor.isMovementDisabled() && !_actor.isCastingNow())
			{
				int ownerX = ((L2Summon) _actor).getOwner().getX();
				int ownerY = ((L2Summon) _actor).getOwner().getY();
				double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - _actor.getY(), ownerX - _actor.getX());

				int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
				int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
				if(!Config.GEODATA_ENABLED || GeoEngine.getInstance().canMoveFromToTarget(_actor.getX(), _actor.getY(), _actor.getZ(), targetX, targetY, _actor.getZ(), _actor.getInstanceId()))
				{
					moveTo(new Location(targetX, targetY, _actor.getZ()));
				}
			}
		}
	}

	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch(getIntention())
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			case AI_INTENTION_IDLE:
			case AI_INTENTION_MOVE_TO:
			case AI_INTENTION_PICK_UP:
				((L2Summon) _actor).setFollowStatus(_startFollow);
		}
	}

	public void notifyAttackModeChange()
	{
		_attackMode = !_attackMode;
		((L2Summon) _actor).setDefendingMode(_attackMode);
	}

	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		_lastAttack = getIntention() == AI_INTENTION_ATTACK ? getAttackTarget() : null;
		super.onIntentionCast(skill, target);
	}

	private void startAvoidTask()
	{
		if(_avoidTask == null)
		{
			_avoidTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 100, 100);
		}
	}

	private void stopAvoidTask()
	{
		if(_avoidTask != null)
		{
			_avoidTask.cancel(false);
			_avoidTask = null;
		}
	}
}