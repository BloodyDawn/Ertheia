package dwo.gameserver.model.actor.ai;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Character.AIAccessor;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2ControllableMobInstance;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.MobGroup;
import dwo.gameserver.model.world.npc.MobGroupTable;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.List;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;

public class L2ControllableMobAI extends L2AttackableAI
{
	public static final int AI_IDLE = 1;
	public static final int AI_NORMAL = 2;
	public static final int AI_FORCEATTACK = 3;
	public static final int AI_FOLLOW = 4;
	public static final int AI_CAST = 5;
	public static final int AI_ATTACK_GROUP = 6;

	private int _alternateAI;

	private boolean _isThinking; // to prevent thinking recursively
	private boolean _isNotMoving;

	private L2Character _forcedTarget;
	private MobGroup _targetGroup;

	public L2ControllableMobAI(AIAccessor accessor)
	{
		super(accessor);
		_alternateAI = AI_IDLE;
	}

	protected void thinkFollow()
	{
		L2Attackable me = (L2Attackable) _actor;
		if(!Util.checkIfInRange(MobGroupTable.FOLLOW_RANGE, me, _forcedTarget, true))
		{
			int signX = Rnd.get(2) == 0 ? -1 : 1;
			int signY = Rnd.get(2) == 0 ? -1 : 1;
			int randX = Rnd.get(MobGroupTable.FOLLOW_RANGE);
			int randY = Rnd.get(MobGroupTable.FOLLOW_RANGE);
			moveTo(new Location(_forcedTarget.getX() + signX * randX, _forcedTarget.getY() + signY * randY, _forcedTarget.getZ()));
		}
	}

	@Override
	protected void onEvtThink()
	{
		if(_isThinking)
		{
			return;
		}

		_isThinking = true;

		try
		{
			switch(_alternateAI)
			{
				case AI_IDLE:
					if(getIntention() != AI_INTENTION_ACTIVE)
					{
						setIntention(AI_INTENTION_ACTIVE);
					}
					break;
				case AI_FOLLOW:
					thinkFollow();
					break;
				case AI_CAST:
					thinkCast();
					break;
				case AI_FORCEATTACK:
					thinkForceAttack();
					break;
				case AI_ATTACK_GROUP:
					thinkAttackGroup();
					break;
				default:
					if(getIntention() == AI_INTENTION_ACTIVE)
					{
						thinkActive();
					}
					else if(getIntention() == AI_INTENTION_ATTACK)
					{
						thinkAttack();
					}
					break;
			}
		}
		finally
		{
			_isThinking = false;
		}
	}

	protected void thinkCast()
	{
		L2Attackable npc = (L2Attackable) _actor;

		if(getAttackTarget() == null || getAttackTarget().isAlikeDead())
		{
			setAttackTarget(findNextRndTarget());
			clientStopMoving(null);
		}

		if(getAttackTarget() == null)
		{
			return;
		}

		npc.setTarget(getAttackTarget());

		if(!_actor.isMuted())
		{
			int max_range = 0;
			// check distant skills

			for(L2Skill sk : _actor.getAllSkills())
			{
				if(Util.checkIfInRange(sk.getCastRange(), _actor, getAttackTarget(), true) && !_actor.isSkillDisabled(sk) && _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk))
				{
					_accessor.doCast(sk);
					return;
				}

				max_range = Math.max(max_range, sk.getCastRange());
			}

			if(!_isNotMoving)
			{
				moveToPawn(getAttackTarget(), max_range);
			}

		}
	}

	protected void thinkAttackGroup()
	{
		L2Character target = _forcedTarget;
		if(target == null || target.isAlikeDead())
		{
			// try to get next group target
			_forcedTarget = findNextGroupTarget();
			clientStopMoving(null);
		}

		if(target == null)
		{
			return;
		}

		_actor.setTarget(target);
		// as a response, we put the target in a forcedattack mode
		L2ControllableMobInstance theTarget = (L2ControllableMobInstance) target;
		L2ControllableMobAI ctrlAi = (L2ControllableMobAI) theTarget.getAI();
		ctrlAi.forceAttack(_actor);

		double dist2 = _actor.getPlanDistanceSq(target.getX(), target.getY());
		int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius(_actor) + target.getTemplate().getCollisionRadius(target);
		int max_range = range;

		if(!_actor.isMuted() && dist2 > (range + 20) * (range + 20))
		{
			// check distant skills
			for(L2Skill sk : _actor.getAllSkills())
			{
				int castRange = sk.getCastRange();

				if(castRange * castRange >= dist2 && !_actor.isSkillDisabled(sk) && _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk))
				{
					_accessor.doCast(sk);
					return;
				}

				max_range = Math.max(max_range, castRange);
			}

			if(!_isNotMoving)
			{
				moveToPawn(target, range);
			}

			return;
		}
		_accessor.doAttack(target);
	}

	protected void thinkForceAttack()
	{
		if(_forcedTarget == null || _forcedTarget.isAlikeDead())
		{
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			_alternateAI = AI_IDLE;
		}

		_actor.setTarget(_forcedTarget);
		double dist2 = _actor.getPlanDistanceSq(_forcedTarget.getX(), _forcedTarget.getY());
		int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius(_actor) + _forcedTarget.getTemplate().getCollisionRadius(_forcedTarget);
		int max_range = range;

		if(!_actor.isMuted() && dist2 > (range + 20) * (range + 20))
		{
			// check distant skills
			for(L2Skill sk : _actor.getAllSkills())
			{
				int castRange = sk.getCastRange();

				if(castRange * castRange >= dist2 && !_actor.isSkillDisabled(sk) && _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk))
				{
					_accessor.doCast(sk);
					return;
				}

				max_range = Math.max(max_range, castRange);
			}

			if(!_isNotMoving)
			{
				moveToPawn(_forcedTarget, _actor.getPhysicalAttackRange()/*range*/);
			}

			return;
		}

		_accessor.doAttack(_forcedTarget);
	}

	protected void thinkAttack()
	{
		if(getAttackTarget() == null || getAttackTarget().isAlikeDead())
		{
			if(getAttackTarget() != null)
			{
				// stop hating
				L2Attackable npc = (L2Attackable) _actor;
				npc.stopHating(getAttackTarget());
			}

			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			// notify aggression
			if(((L2Npc) _actor).getFactionId() != null)
			{
				String faction_id = ((L2Npc) _actor).getFactionId();

				for(L2Object obj : _actor.getKnownList().getKnownObjects().values())
				{
					if(!(obj instanceof L2Npc))
					{
						continue;
					}

					L2Npc npc = (L2Npc) obj;

					if(!faction_id.equals(npc.getFactionId()))
					{
						continue;
					}

					if(_actor.isInsideRadius(npc, npc.getFactionRange(), false, true) && Math.abs(getAttackTarget().getZ() - npc.getZ()) < 200)
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					}
				}
			}

			_actor.setTarget(getAttackTarget());
			double dist2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
			int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius(_actor) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget());
			int max_range = range;

			if(!_actor.isMuted() && dist2 > (range + 20) * (range + 20))
			{
				// check distant skills
				for(L2Skill sk : _actor.getAllSkills())
				{
					int castRange = sk.getCastRange();

					if(castRange * castRange >= dist2 && !_actor.isSkillDisabled(sk) && _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk))
					{
						_accessor.doCast(sk);
						return;
					}

					max_range = Math.max(max_range, castRange);
				}

				moveToPawn(getAttackTarget(), range);
				return;
			}

			// Force mobs to attack anybody if confused.
			L2Character hated;

			hated = _actor.isConfused() ? findNextRndTarget() : getAttackTarget();

			if(hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}

			if(!_actor.isMuted() && Rnd.get(5) == 3)
			{
				for(L2Skill sk : _actor.getAllSkills())
				{
					int castRange = sk.getCastRange();

					if(castRange * castRange >= dist2 && !_actor.isSkillDisabled(sk) && _actor.getCurrentMp() < _actor.getStat().getMpConsume(sk))
					{
						_accessor.doCast(sk);
						return;
					}
				}
			}
			_accessor.doAttack(getAttackTarget());
		}
	}

	private void thinkActive()
	{
		setAttackTarget(findNextRndTarget());
		L2Character hated;

		hated = _actor.isConfused() ? findNextRndTarget() : getAttackTarget();

		if(hated != null)
		{
			_actor.setRunning();
			setIntention(AI_INTENTION_ATTACK, hated);
		}
	}

	private boolean autoAttackCondition(L2Character target)
	{
		if(target == null || !(_actor instanceof L2Attackable))
		{
			return false;
		}
		L2Attackable me = (L2Attackable) _actor;

		if(target instanceof L2NpcInstance || target instanceof L2DoorInstance)
		{
			return false;
		}

		if(target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || Math.abs(_actor.getZ() - target.getZ()) > 100)
		{
			return false;
		}

		// Check if the target isn't invulnerable
		if(target.isInvul())
		{
			return false;
		}

		// Spawn protection (only against mobs)
		if(target instanceof L2PcInstance && ((L2PcInstance) target).isSpawnProtected())
		{
			return false;
		}

		// Check if the target is a L2PlayableInstance
		if(target instanceof L2Playable)
		{
			// Check if the target isn't in silent move mode
			if(((L2Playable) target).isSilentMoving())
			{
				return false;
			}
		}

		if(target instanceof L2Npc)
		{
			return false;
		}
		return me.isAggressive();
	}

	private L2Character findNextRndTarget()
	{
		int aggroRange = ((L2Attackable) _actor).getAggroRange();
		L2Attackable npc = (L2Attackable) _actor;
		int npcX;
		int npcY;
		int targetX;
		int targetY;
		double dy;
		double dx;
		double dblAggroRange = aggroRange * aggroRange;

		List<L2Character> potentialTarget = new FastList<>();

		for(L2Object obj : npc.getKnownList().getKnownObjects().values())
		{
			if(!(obj instanceof L2Character))
			{
				continue;
			}

			npcX = npc.getX();
			npcY = npc.getY();
			targetX = obj.getX();
			targetY = obj.getY();

			dx = npcX - targetX;
			dy = npcY - targetY;

			if(dx * dx + dy * dy > dblAggroRange)
			{
				continue;
			}

			L2Character target = (L2Character) obj;

			if(autoAttackCondition(target))
			{
				potentialTarget.add(target);
			}
		}

		if(potentialTarget.isEmpty())
		{
			return null;
		}

		// we choose a random target
		int choice = Rnd.get(potentialTarget.size());

		return potentialTarget.get(choice);
	}

	private L2ControllableMobInstance findNextGroupTarget()
	{
		return _targetGroup.getRandomMob();
	}

	public int getAlternateAI()
	{
		return _alternateAI;
	}

	public void setAlternateAI(int _alternateai)
	{
		_alternateAI = _alternateai;
	}

	public void forceAttack(L2Character target)
	{
		_alternateAI = AI_FORCEATTACK;
		_forcedTarget = target;
	}

	public void forceAttackGroup(MobGroup group)
	{
		_forcedTarget = null;
		_targetGroup = group;
		_alternateAI = AI_ATTACK_GROUP;
	}

	public void stop()
	{
		_alternateAI = AI_IDLE;
		clientStopMoving(null);
	}

	public void move(Location loc)
	{
		moveTo(loc);
	}

	public void follow(L2Character target)
	{
		_alternateAI = AI_FOLLOW;
		_forcedTarget = target;
	}

	public boolean isThinking()
	{
		return _isThinking;
	}

	public void setThinking(boolean isThinking)
	{
		_isThinking = isThinking;
	}

	public boolean isNotMoving()
	{
		return _isNotMoving;
	}

	public void setNotMoving(boolean isNotMoving)
	{
		_isNotMoving = isNotMoving;
	}

	private L2Character getForcedTarget()
	{
		return _forcedTarget;
	}

	private void setForcedTarget(L2Character forcedTarget)
	{
		_forcedTarget = forcedTarget;
	}

	private MobGroup getGroupTarget()
	{
		return _targetGroup;
	}

	private void setGroupTarget(MobGroup targetGroup)
	{
		_targetGroup = targetGroup;
	}
}
