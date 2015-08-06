package dwo.gameserver.model.actor.ai;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Decoy;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.AutoAttackStart;
import dwo.gameserver.network.game.serverpackets.AutoAttackStop;
import dwo.gameserver.network.game.serverpackets.Die;
import dwo.gameserver.network.game.serverpackets.FinishRotating;
import dwo.gameserver.network.game.serverpackets.MTL;
import dwo.gameserver.network.game.serverpackets.MoveToPawn;
import dwo.gameserver.network.game.serverpackets.StopMove;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.Future;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_IDLE;

/**
 * Mother class of all objects AI in the world.
 * AbastractAI : L2CharacterAI
 */

public abstract class AbstractAI implements Ctrl
{
	protected static final Logger _log = LogManager.getLogger(AbstractAI.class);
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	/**
	 * The character that this AI manages
	 */
	protected final L2Character _actor;
	/**
	 * An accessor for private methods of the actor
	 */
	protected final L2Character.AIAccessor _accessor;
	/**
	 * Current long-term intention
	 */
	protected CtrlIntention _intention = AI_INTENTION_IDLE;
	/**
	 * Current long-term intention parameter
	 */
	protected Object _intentionArg0;
	/**
	 * Current long-term intention parameter
	 */
	protected Object _intentionArg1;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected volatile boolean _clientMoving;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected volatile boolean _clientAutoAttacking;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected int _clientMovingToPawnOffset;
	protected L2Character _attackTarget;
	protected L2Character _followTarget;
	protected Future<?> _followTask;
	/**
	 * The skill we are currently casting by INTENTION_CAST
	 */
	L2Skill _skill;
	private NextAction _nextAction;
	/**
	 * Different targets this AI maintains
	 */
	private L2Object _target;
	private L2Character _castTarget;
	/**
	 * Different internal state flags
	 */
	private int _moveToPawnTimeout;

	/**
	 * Constructor of AbstractAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;

		// Get the L2Character managed by this Accessor AI
		_actor = accessor.getActor();
	}

	/**
	 * @return the _nextAction
	 */
	public NextAction getNextAction()
	{
		return _nextAction;
	}

	/**
	 * @param nextAction the next action to set.
	 */
	public void setNextAction(NextAction nextAction)
	{
		_nextAction = nextAction;
	}

	public L2Character.AIAccessor getAccessor()
	{
		return _accessor;
	}

	/**
	 * Return the L2Character managed by this Accessor AI.<BR><BR>
	 */
	@Override
	public L2Character getActor()
	{
		return _actor;
	}

	/**
	 * @return the current Intention.
	 */
	@Override
	public CtrlIntention getIntention()
	{
		return _intention;
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.
	 * Caution : Stop the FOLLOW mode if necessary
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}

	/**
	 * @return current attack target.
	 */
	@Override
	public L2Character getAttackTarget()
	{
		return _attackTarget;
	}

	protected void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.
	 * Caution : Stop the FOLLOW mode if necessary
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 */
	@Override
	public void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.
	 * Caution : Stop the FOLLOW mode if necessary
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 * @param arg1      The second parameter of the Intention (optional target)
	 */
	/*
	 public final void informAIIntention(CtrlIntention intent, Object arg0) {
         ThreadPoolManager.getInstance().executeAi(new InformAIMsg(this, intent, arg0));
     }

     public final void informAIIntention(CtrlIntention intent) {
     ThreadPoolManager.getInstance().executeAi(new InformAIMsg(this, intent, null));
     }

     public class InformAIMsg implements Runnable {
private AbstractAI _ai;
         private CtrlIntention _intent;
         private Object _arg0;
         public InformAIMsg(AbstractAI ai, CtrlIntention intention, Object arg0) {
             _ai=ai;
             _intent = intention;
             _arg0 = arg0;
         }
  public final void run() {
             _ai.setIntention(_intent, _arg0, null);
         }
     }
      */
	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		// Stop the follow mode if necessary
		if(intention != AI_INTENTION_FOLLOW && intention != AI_INTENTION_ATTACK)
		{
			stopFollow();
		}

		//Launch the onIntention method of the L2CharacterAI corresponding to the new Intention
		switch(intention)
		{
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((Location) arg0);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}
		// If do move or follow intention drop next action.
		if(_nextAction != null && _nextAction.getIntentions().contains(intention))
		{
			_nextAction = null;
		}
	}

	/**
	 * Launch the L2CharacterAI onEvt methodcorresponding to the Event.
	 * Caution: The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.
	 * Caution: The current general intention won't be change
	 * (ex : Ifthe character attack and is stunned, he will attack again after the stunned period)
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event(optional target)
	 */
	@Override
	public void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.
	 * Caution: The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if(!_actor.isVisible() && !_actor.isTeleporting() || !_actor.hasAI())
		{
			return;
		}

		switch(evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_PARALYZED:
				onEvtParalyzed((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_FLYUP:
				onEvtFlyUp((L2Character) arg0);
				break;
			case EVT_KNOCK_DOWN:
				onEvtKnockDown((L2Character) arg0);
				break;
			case EVT_KNOCK_BACK:
				onEvtKnockBack((L2Character) arg0);
				break;
			case EVT_EVADED:
				onEvtEvaded((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				if(!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtReadyToAct();
				}
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				// happens e.g. from stopmove but we don't process it if we're casting
				if(!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtArrived();
				}
				break;
			case EVT_ARRIVED_REVALIDATE:
				// this is disregarded if the char is not moving any more
				if(_actor.isMoving())
				{
					onEvtArrivedRevalidate();
				}
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((Location) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}

		// Do next action.
		if(_nextAction != null && _nextAction.getEvents().contains(evt))
		{
			_nextAction.doAction();
		}
	}

	/**
	 * @return the current cast target.
	 */
	public L2Character getCastTarget()
	{
		return _castTarget;
	}

	protected void setCastTarget(L2Character target)
	{
		_castTarget = target;
	}

	/**
	 * Set the Intention of this AbstractAI.
	 * Caution: This method is USED by AI classes
	 * Overridden in:
	 * L2AttackableAI : Create an AI Task executed every 1s (if necessary)
	 * L2PlayerAI	  : Stores the current AI intention parameters to later restore it if necessary
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		synchronized(this)
		{
			_intention = intention;
			_intentionArg0 = arg0;
			_intentionArg1 = arg1;
		}
	}

    /*
     public final void informAIEvent(CtrlEvent evt) {
    ThreadPoolManager.getInstance().executeAi(newInformAIEvent(this, evt, null, null));
     }
 
     public final void informAIEvent(CtrlEvent evt, Object arg0) {
         ThreadPoolManager.getInstance().executeAi(new InformAIEvent(this, evt, arg0, null));
     }
 
     public final void informAIEvent(CtrlEvent evt, Object arg0, Object arg1) {
         ThreadPoolManager.getInstance().executeAi(new InformAIEvent(this, evt, arg0, arg1));
     }
 
     public class InformAIEvent implements Runnable {
         private AbstractAI _ai;
         private CtrlEvent _evt;
         private Object _arg0, _arg1;
 
         public InformAIEvent(AbstractAI ai,CtrlEvent evt, Object arg0, Object arg1) {
             _ai=ai;
     _evt= evt;
             _arg0 = arg0;
             _arg1 = arg1;
         }
 
         public final void run() {
            _ai.notifyEvent(_evt, _arg0, _arg1);
     }
     }
      */

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(L2Character target);

	protected abstract void onIntentionCast(L2Skill skill, L2Object target);

	protected abstract void onIntentionMoveTo(Location destination);

	protected abstract void onIntentionFollow(L2Character target);

	protected abstract void onIntentionPickUp(L2Object item);

	protected abstract void onIntentionInteract(L2Object object);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(L2Character attacker);

	protected abstract void onEvtAggression(L2Character target, int aggro);

	protected abstract void onEvtStunned(L2Character attacker);

	protected abstract void onEvtParalyzed(L2Character attacker);

	protected abstract void onEvtSleeping(L2Character attacker);

	protected abstract void onEvtRooted(L2Character attacker);

	protected abstract void onEvtConfused(L2Character attacker);

	protected abstract void onEvtMuted(L2Character attacker);

	protected abstract void onEvtFlyUp(L2Character attacker);

	protected abstract void onEvtKnockDown(L2Character attacker);

	protected abstract void onEvtKnockBack(L2Character attacker);

	protected abstract void onEvtEvaded(L2Character attacker);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtUserCmd(Object arg0, Object arg1);

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedRevalidate();

	protected abstract void onEvtArrivedBlocked(Location blocked_at_pos);

	protected abstract void onEvtForgetObject(L2Object object);

	protected abstract void onEvtCancel();

	protected abstract void onEvtDead();

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting();

	/**
	 * Cancel action client side by sending ServerMode->Client packet ActionFail to the L2PcInstance actor.<BR><BR>
	 * <p/>
	 * Caution: Low level function, used byAI subclasses
	 */
	protected void clientActionFailed()
	{
		if(_actor instanceof L2PcInstance)
		{
			_actor.sendActionFailed();
		}
	}

	/**
	 * Move the actor to Pawn server side AND client side by sending ServerMode->Client packet MoveToPawn (broadcast).
	 * Caution: Low level function, used by AI subclasses
	 * @param pawn pawn
	 * @param offset offset
	 */
	protected void moveToPawn(L2Object pawn, int offset)
	{
		// Check if actor can move
		if(_actor.isMovementDisabled())
		{
			_actor.sendActionFailed();
		}
		else
		{
			if(offset < 10)
			{
				offset = 10;
			}

			// preventpossible extra calls to this function (there is none?),
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if(_clientMoving && _target == pawn)
			{
				if(_clientMovingToPawnOffset == offset)
				{
					if(GameTimeController.getInstance().getGameTicks() < _moveToPawnTimeout)
					{
						return;
					}
					sendPacket = false;
				}
				else if(_actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if(GameTimeController.getInstance().getGameTicks() < _moveToPawnTimeout + 10)
					{
						return;
					}
				}
			}

			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_target = pawn;
			_moveToPawnTimeout = GameTimeController.getInstance().getGameTicks();
			_moveToPawnTimeout += 1000 / GameTimeController.MILLIS_IN_TICK;

			if(pawn == null || _accessor == null)
			{
				return;
			}

			// "Rounding" player with summons
			int x = pawn.getX();
			int y = pawn.getY();
			int z = pawn.getZ();
			boolean rounding = false;
			if(pawn instanceof L2PcInstance && (_actor instanceof L2Summon || _actor instanceof L2Decoy) && _intention == AI_INTENTION_FOLLOW)
			{
				sendPacket = true;
				List<?> pets = null;
				if(_actor instanceof L2Summon)
				{
					pets = ((L2Summon) _actor).getOwner().getPets();
				}
				else if(_actor instanceof L2Decoy)
				{
					pets = ((L2Decoy) _actor).getOwner().getDecoy();
				}

				// Only if pets count greater than 1, 'coz we don't need to manage single summon coords
				if(pets != null && pets.size() > 1)
				{
					rounding = true;
					short index = 0;
					for(L2Object pet : (List<L2Object>) pets)
					{
						if(pet.getObjectId() == _actor.getObjectId())
						{
							break;
						}
						++index;
					}

					double course = 0.0;

					switch(index)
					{
						case 0:
							course = -35;
							break;
						case 1:
							course = 35;
							break;
						case 2:
							course = 90;
							break;
						case 3:
							course = -90;
							break;
					}

					double angle = Util.convertHeadingToDegree(((L2PcInstance) pawn).getHeading());
					double radians = Math.toRadians(angle);
					double radius = _intention == AI_INTENTION_FOLLOW ? -100.0 : -35;

					// TODO: Cache cos/sin calls to array.
					int x1 = (int) (Math.cos(Math.PI + radians + course) * radius);
					int y1 = (int) (Math.sin(Math.PI + radians + course) * radius);

					x = pawn.getX() + x1;
					y = pawn.getY() + y1;

					if(Config.GEODATA_ENABLED)
					{
						Location destiny = GeoEngine.getInstance().moveCheck(pawn.getX(), pawn.getY(), pawn.getZ(), x, y, z, pawn.getInstanceId());
						x = destiny.getX();
						y = destiny.getY();
						z = destiny.getZ();
					}
					offset = 0;
				}
			}

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(x, y, z, offset);

			if(!_actor.isMoving())
			{
				_actor.sendActionFailed();
				return;
			}

			// Send a ServerMode->Client packet MoveToPawn/CharMoveToLocation to the actor and all L2PcInstancein its _knownPlayers
			if(pawn instanceof L2Character)
			{
				if(_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new MTL(_actor), 5000);
					_clientMovingToPawnOffset = 0;
				}
				else if(sendPacket) // don't repeat unnecessarily
				{
					if(rounding)
					{
						_actor.broadcastPacket(new MTL(_actor), 5000);
					}
					else
					{
						_actor.broadcastPacket(new MoveToPawn(_actor, (L2Character) pawn, offset), 5000);
					}
				}
			}
			else
			{
				_actor.broadcastPacket(new MTL(_actor), 5000);
			}
		}
	}

	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending ServerMode->Client packet CharMoveToLocation (broadcast).
	 * Caution: Low level function, used by AI subclasses
	 * @param loc координаты
	 */
	protected void moveTo(Location loc)
	{
		// Check if actor can move
		if(_actor.isMovementDisabled())
		{
			_actor.sendActionFailed();
		}
		else
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(loc.getX(), loc.getY(), loc.getZ());

			// Send a ServerMode->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new MTL(_actor));

		}
	}

	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast).
	 * Caution: Low level function, used by AI subclasses
	 * @param pos координаты
	 */
	protected void clientStopMoving(Location pos)
	{
		// Stop movement of the L2Character
		if(_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}

		_clientMovingToPawnOffset = 0;

		if(_clientMoving || pos != null)
		{
			_clientMoving = false;

			// Send a Server->Client packet StopMove to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new StopMove(_actor));

			if(pos != null)
			{
				// Send a Server->Client packet StopRotation to the actor and all L2PcInstance in its _knownPlayers
				_actor.broadcastPacket(new FinishRotating(_actor.getObjectId(), pos.getHeading(), 0));
			}
		}
	}

	protected void clientStoppedMoving()
	{
		if(_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
	}

	// Client has already arrived to target, no need to force StopMove packet

	public boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}

	public void setAutoAttacking(boolean isAutoAttacking)
	{
		if(_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if(summon.getOwner() != null)
			{
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
			return;
		}
		_clientAutoAttacking = isAutoAttacking;
	}

	/**
	 * Start the actor Auto Attack client side by sending ServerMode->Client packet AutoAttackStart(broadcast).
	 * Caution: Low level function, used by AI subclasses
	 */
	public void clientStartAutoAttack()
	{
		if(_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if(summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}
		if(!_clientAutoAttacking)
		{
			if(_actor instanceof L2PcInstance)
			{
				if(!_actor.getPets().isEmpty())
				{
					for(L2Summon pet : _actor.getPets())
					{
						pet.broadcastPacket(new AutoAttackStart(pet.getObjectId()));
					}
				}
			}
			// Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}

	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast).
	 * Caution: Low level function, used by AI subclasses
	 */
	public void clientStopAutoAttack()
	{
		if(_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if(summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		if(_actor instanceof L2PcInstance)
		{
			if(!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor) && _clientAutoAttacking)
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if(_clientAutoAttacking)
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}

	/**
	 * Kill the actor client side by sending ServerMode->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast).
	 * Caution: Low level function, used by AI subclasses
	 */
	protected void clientNotifyDead()
	{
		// Send a ServerMode->Client packet Die to the actor andall L2PcInstance in its _knownPlayers
		_actor.broadcastPacket(new Die(_actor));

		// Init AI
		_intention = AI_INTENTION_IDLE;
		_target = null;
		_castTarget = null;
		_attackTarget = null;

		// Cancel the followtask if necessary
		stopFollow();
	}

	/**
	 * Update the state of this actor client side by sending ServerMode->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance player.
	 * Caution: Low level function, used byAI subclasses
	 * @param player The L2PcIstance to notify with state of this L2Character
	 */
	public void describeStateToPlayer(L2PcInstance player)
	{
		if(_clientMoving)
		{
			if(_clientMovingToPawnOffset != 0 && _followTarget != null)
			{
				player.sendPacket(new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset));
			}
			else
			{
				player.sendPacket(new MTL(_actor));
			}
		}
	}

	/**
	 * Create and Launchan AI Follow Task to execute every 1s.
	 * @param target The L2Character to follow
	 */
	public void startFollow(L2Character target)
	{
		synchronized(this)
		{
			if(_followTask != null)
			{
				_followTask.cancel(false);
				_followTask = null;
			}

			// Create and Launch an AI Follow Task to execute every 1s
			_followTarget = target;
			_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
		}
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.
	 * @param target The L2Character to follow
	 * @param range specified range
	 */
	public void startFollow(L2Character target, int range)
	{
		synchronized(this)
		{
			if(_followTask != null)
			{
				_followTask.cancel(false);
				_followTask = null;
			}

			_followTarget = target;
			_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
		}
	}

	/**
	 * Stop an AI Follow Task.
	 */
	public void stopFollow()
	{
		synchronized(this)
		{
			if(_followTask != null)
			{
				// Stop the Follow Task
				_followTask.cancel(false);
				_followTask = null;
			}
			_followTarget = null;
		}
	}

	public L2Character getFollowTarget()
	{
		return _followTarget;
	}

	protected L2Object getTarget()
	{
		return _target;
	}

	protected void setTarget(L2Object target)
	{
		_target = target;
	}

	/**
	 * Stop allAi tasks and futures.
	 */
	public void stopAITask()
	{
		stopFollow();
	}

	@Override
	public String toString()
	{
		if(_actor == null)
		{
			return "Actor: null";
		}
		return "Actor: " + _actor;
	}

	private class FollowTask implements Runnable
	{
		protected int _range = 70;

		public FollowTask()
		{
		}

		public FollowTask(int range)
		{
			_range = range;
		}

		@Override
		public void run()
		{
			try
			{
				if(_followTask == null)
				{
					return;
				}

				L2Character followTarget = _followTarget; // copy to prevent NPE
				if(followTarget == null)
				{
					if(_actor instanceof L2Summon)
					{
						((L2Summon) _actor).setFollowStatus(false);
					}
					setIntention(AI_INTENTION_IDLE);
					return;
				}

				if(!_actor.isInsideRadius(followTarget, _range, true, false))
				{
					if(!_actor.isInsideRadius(followTarget, 3000, true, false))
					{
						// if the target is too far (maybe also teleported)
						if(_actor instanceof L2Summon)
						{
							((L2Summon) _actor).setFollowStatus(false);
						}

						setIntention(AI_INTENTION_IDLE);
						return;
					}

					// Lock follow task, if summon is spawning (spawn animation active) at this time.
					if(_actor instanceof L2Summon && ((L2Summon) _actor).isSpawningNow())
					{
						_actor.sendActionFailed();
						return;
					}

					moveToPawn(followTarget, _range);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}
