package dwo.gameserver.model.actor.ai;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Character.AIAccessor;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAbnormalStatusUpdateFromTarget;
import org.apache.log4j.Level;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_CAST;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_IDLE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_REST;

public class L2PlayerAI extends L2PlayableAI
{
	IntentionCommand _nextIntention;
	private boolean _thinking; // to prevent recursive thinking

	public L2PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}

	void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}

	@Override
	public IntentionCommand getNextIntention()
	{
		return _nextIntention;
	}

	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}

	@Override
	protected void onIntentionRest()
	{
		if(getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			if(getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			clientStopMoving(null);
		}
	}

	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to
	 * Location Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending
	 * ServerMode->Client packet AutoAttackStop (broadcast)</li> <li>Set the
	 * Intention of this AI to AI_INTENTION_MOVE_TO</li> <li>Move the actor to
	 * Location (x,y,z) server side AND client side by sending ServerMode->Client
	 * packet CharMoveToLocation (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionMoveTo(Location pos)
	{
		if(getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending ServerMode->Client packet
			// ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isImmobilized() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			saveNextIntention(AI_INTENTION_MOVE_TO, pos, null);
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);

		// Stop the actor auto-attack client side by sending ServerMode->Client
		// packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Abort the attack of the L2Character and send ServerMode->Client
		// ActionFail packet
		_actor.abortAttack();

		// Move the actor to Location (x,y,z) server side AND client side by
		// sending ServerMode->Client packet CharMoveToLocation (broadcast)
		moveTo(pos);
	}

	@Override
	protected void onEvtThink()
	{
		if(_thinking && getIntention() != AI_INTENTION_CAST) // casting must always continue
		{
			return;
		}

		_thinking = true;
		try
		{
			if(getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if(getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if(getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if(getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		if(_nextIntention != null)
		{
			setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
			_nextIntention = null;
		}
		super.onEvtReadyToAct();
	}

	/**
	 * Launch actions corresponding to the Event Cancel.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop an AI Follow Task</li> <li>Launch actions corresponding to the
	 * Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtCancel()
	{
		_nextIntention = null;
		super.onEvtCancel();
	}

	/**
	 * Finalize the casting of a skill. This method overrides L2CharacterAI
	 * method.<BR>
	 * <BR>
	 * <B>What it does:</B>
	 * Check if actual intention is set to CAST and, if so, retrieves latest
	 * intention
	 * before the actual CAST and set it as the current intention for the player
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		if(getIntention() == AI_INTENTION_CAST)
		{
			// run interrupted or next intention

			IntentionCommand nextIntention = _nextIntention;
			if(nextIntention != null)
			{
				if(nextIntention._crtlIntention == AI_INTENTION_CAST)
				{
					setIntention(AI_INTENTION_IDLE);
				}
				else
				{
					setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
				}
			}
			else
			{
				// set intention to idle if skill doesn't change intention.
				setIntention(AI_INTENTION_IDLE);
			}
		}
	}

	/**
	 * Saves the current Intention for this L2PlayerAI if necessary and calls
	 * changeIntention in AbstractAI.<BR>
	 * <BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		synchronized(this)
		{
			// do nothing unless CAST intention
			// however, forget interrupted actions when starting to use an offensive
			// skill
			if(intention != AI_INTENTION_CAST || arg0 != null && ((L2Skill) arg0).isOffensive())
			{
				_nextIntention = null;
				super.changeIntention(intention, arg0, arg1);
				return;
			}

			// do nothing if next intention is same as current one.
			if(intention == _intention && arg0.equals(_intentionArg0) && arg1.equals(_intentionArg1))
			{
				super.changeIntention(intention, arg0, arg1);
				return;
			}

			// save current intention so it can be used after cast
			saveNextIntention(_intention, _intentionArg0, _intentionArg1);
			super.changeIntention(intention, arg0, arg1);
		}
	}

	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;

		super.clientNotifyDead();
	}

	private void thinkAttack()
	{
		L2Character target = getAttackTarget();
		if(target == null)
		{
			return;
		}
		if(checkTargetLostOrDead(target))
		{
			// Notify the target
			setAttackTarget(null);
			return;
		}
		if(maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			return;
		}

		_accessor.doAttack(target);
	}

	private void thinkCast()
	{
		L2Character target = getCastTarget();
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "L2PlayerAI: thinkCast -> Start");
		}

		if(_skill.getTargetType() == L2TargetType.TARGET_GROUND && _actor instanceof L2PcInstance)
		{
			if(maybeMoveToPosition(((L2PcInstance) _actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
			{
				if(_actor.isDoubleCastingNow())
				{
					_actor.setIsDoubleCastingNow(false);
				}
				else
				{
					_actor.setIsCastingNow(false);
				}
				return;
			}
		}
		else
		{
			if(checkTargetLost(target))
			{
				if(_skill.isOffensive() && getAttackTarget() != null)
				{
					// Notify the target
					setCastTarget(null);
				}
				if(_actor.isDoubleCastingNow())
				{
					_actor.setIsDoubleCastingNow(false);
				}
				else
				{
					_actor.setIsCastingNow(false);
				}
				return;
			}
			if(target != null && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setIsCastingNow(false);
				_actor.setIsDoubleCastingNow(false);
				return;
			}
		}

		// Если каст симмуллируется, то не останавливаем движение персонажа
		if(_skill.getHitTime() > 50 && !_skill.isSimultaneousCast())
		{
			clientStopMoving(null);
		}

		L2Object oldTarget = _actor.getTarget();
		if(oldTarget != null && target != null && !oldTarget.equals(target))
		{
			// Replace the current target by the cast target
			_actor.setTarget(getCastTarget());
			// Launch the Cast of the skill
			_accessor.doCast(_skill);
			// Restore the initial target
			_actor.setTarget(oldTarget);
			if(oldTarget instanceof L2Character)
			{
				_actor.sendPacket(new ExAbnormalStatusUpdateFromTarget((L2Character) oldTarget, _actor.isAwakened()));
			}
		}
		else
		{
			_accessor.doCast(_skill);
		}
	}

	private void thinkPickUp()
	{
		if(_actor.isImmobilized() || _actor.isCastingNow())
		{
			return;
		}
		L2Object target = getTarget();
		if(checkTargetLost(target))
		{
			return;
		}
		if(maybeMoveToPawn(target, 36))
		{
			return;
		}
		setIntention(AI_INTENTION_IDLE);
		((L2PcInstance.AIAccessor) _accessor).doPickupItem(target);
	}

	private void thinkInteract()
	{
		if(_actor.isImmobilized() || _actor.isCastingNow())
		{
			return;
		}
		L2Object target = getTarget();
		if(checkTargetLost(target))
		{
			return;
		}
		if(maybeMoveToPawn(target, 36))
		{
			return;
		}
		if(!(target instanceof L2StaticObjectInstance))
		{
			((L2PcInstance.AIAccessor) _accessor).doInteract((L2Character) target);
		}
		setIntention(AI_INTENTION_IDLE);
	}
}
