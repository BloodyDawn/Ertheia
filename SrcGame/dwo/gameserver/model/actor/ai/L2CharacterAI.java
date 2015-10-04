/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor.ai;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.AutoAttackStop;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.geometry.Point3D;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_CAST;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_IDLE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_REST;

/**
 * This class manages AI of L2Character.<BR><BR>
 * <p/>
 * L2CharacterAI :<BR><BR>
 * <li>L2AttackableAI</li>
 * <li>L2DoorAI</li>
 * <li>L2PlayerAI</li>
 * <li>L2SummonAI</li><BR><BR>
 */
public class L2CharacterAI extends AbstractAI
{
	/**
	 * Constructor of L2CharacterAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2CharacterAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}

	public IntentionCommand getNextIntention()
	{
		return null;
	}

	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE </li>
	 * <li>Init cast and attack target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast) </li>
	 * <li>Stand up the actor server side AND client side by sending Server->Client packet ChangeWaitType (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		changeIntention(AI_INTENTION_IDLE, null, null);

		// Init cast and attack target
		setCastTarget(null);
		setAttackTarget(null);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

	}

	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : <I>if the Intention is not already Active</I></B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_ACTIVE </li>
	 * <li>Init cast and attack target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast) </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 */
	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if(getIntention() != AI_INTENTION_ACTIVE)
		{
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);

			// Init cast and attack target
			setCastTarget(null);
			setAttackTarget(null);

			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();

			// Also enable random animations for this L2Character if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if(_actor instanceof L2Attackable)
			{
				((L2Npc) _actor).startRandomAnimationTimer();
			}

			// Launch the Think Event
			onEvtThink();
		}
	}

	/**
	 * Manage the Rest Intention.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE </li><BR><BR>
	 */
	@Override
	protected void onIntentionRest()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(AI_INTENTION_IDLE);
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_ATTACK </li>
	 * <li>Set or change the AI attack target </li>
	 * <li>Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart (broadcast) </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 * <p/>
	 * <p/>
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2AttackableAI : Calculate attack timeout</li><BR><BR>
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if(target == null)
		{
			clientActionFailed();
			return;
		}

		if(getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Check if the Intention is already AI_INTENTION_ATTACK
		if(getIntention() == AI_INTENTION_ATTACK)
		{
			// Check if the AI already targets the L2Character
			if(getAttackTarget() == target)
			{
				clientActionFailed(); // else client freezes until cancel target
			}
			else
			{
				// Set the AI attack target (change target)
				setAttackTarget(target);

				stopFollow();

				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);

			}
		}
		else
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ATTACK
			changeIntention(AI_INTENTION_ATTACK, target, null);

			// Set the AI attack target
			setAttackTarget(target);

			stopFollow();

			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}

	/**
	 * Manage the Cast Intention : Stop current Attack, Init the AI in order to cast and Launch Think Event.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI cast target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor </li>
	 * <li>Set the AI skill used by INTENTION_CAST </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_CAST </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 */
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if(getIntention() == AI_INTENTION_REST && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			_actor.setIsDoubleCastingNow(false);
			return;
		}

		// Set the AI cast target
		setCastTarget((L2Character) target);

		// Stop actions client-side to cast the skill
		if(skill.getHitTime() > 50)
		{
			// Abort the attack of the L2Character and send Server->Client ActionFail packet
			_actor.abortAttack();

			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			// no need for second ActionFail packet, abortAttack() already sent it
			//clientActionFailed();
		}

		// Set the AI skill used by INTENTION_CAST
		_skill = skill;

		// Change the Intention of this AbstractAI to AI_INTENTION_CAST
		changeIntention(AI_INTENTION_CAST, skill, target);

		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO </li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionMoveTo(Location pos)
	{
		if(getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Abort the attack of the L2Character and send Server->Client ActionFail packet
		_actor.abortAttack();

		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(pos);
	}

	/**
	 * Manage the Follow Intention : Stop current Attack and Launch a Follow Task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_FOLLOW </li>
	 * <li>Create and Launch an AI Follow Task to execute every 1s </li><BR><BR>
	 */
	@Override
	protected void onIntentionFollow(L2Character target)
	{
		if(getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isMovementDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Dead actors can`t follow
		if(_actor.isDead())
		{
			clientActionFailed();
			return;
		}

		// do not follow yourself
		if(_actor.equals(target))
		{
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Set the Intention of this AbstractAI to AI_INTENTION_FOLLOW
		changeIntention(AI_INTENTION_FOLLOW, target, null);

		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}

	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI pick up target </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_PICK_UP </li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		if(getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isImmobilized() || _actor.isCastingNow()) // _actor.isAllSkillsDisabled()
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		if(object instanceof L2ItemInstance && ((L2ItemInstance) object).getItemLocation() != ItemLocation.VOID)
		{
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_PICK_UP
		changeIntention(AI_INTENTION_PICK_UP, object, null);

		// Set the AI pick up target
		setTarget(object);
		if(object.getX() == 0 && object.getY() == 0) // TODO: Find the drop&spawn bug
		{
			_log.log(Level.WARN, "Object in coords 0,0 - using a temporary fix");
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}

		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}

	/**
	 * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the AI interact target </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_INTERACT </li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast) </li><BR><BR>
	 *
	 */
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if(getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if(_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFail to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		if(getIntention() != AI_INTENTION_INTERACT)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_INTERACT
			changeIntention(AI_INTENTION_INTERACT, object, null);

			// Set the AI interact target
			setTarget(object);

			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// do nothing
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if(attacker instanceof L2Attackable && !attacker.isCoreAIDisabled())
		{
			clientStartAutoAttack();
		}
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event Stunned then onAttacked Event.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFail packet and a System Message to the L2Character </li>
	 * <li>Break a cast and send Server->Client ActionFail packet and a System Message to the L2Character </li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode) </li><BR><BR>
	 */
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtParalyzed(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Sleeping.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFail packet and a System Message to the L2Character </li>
	 * <li>Break a cast and send Server->Client ActionFail packet and a System Message to the L2Character </li><BR><BR>
	 */
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		// stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}

	/**
	 * Launch actions corresponding to the Event Rooted.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR><BR>
	 */
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		//_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		//if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		//    AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Confused.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR><BR>
	 */
	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Muted.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Break a cast and send Server->Client ActionFail packet and a System Message to the L2Character </li><BR><BR>
	 */
	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		// Break a cast and send Server->Client ActionFail packet and a System Message to the L2Character
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtFlyUp(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtKnockDown(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtKnockBack(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtEvaded(L2Character attacker)
	{
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event Arrived.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtArrived()
	{
		_accessor.getActor().revalidateZone(true);

		if(_accessor.getActor().moveToNextRoutePoint())
		{
			return;
		}

		if(_accessor.getActor() instanceof L2Attackable)
		{
			((L2Attackable) _accessor.getActor()).setisReturningToSpawnPoint(false);
		}
		clientStoppedMoving();

		if(_actor instanceof L2Npc)
		{
			L2Npc npc = (L2Npc) _actor;
			WalkingManager.getInstance().onArrived(npc); // Walking Manager support

			// Notify quest
			if(npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_MOVE_FINISHED) != null)
			{
				for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_MOVE_FINISHED))
				{
					quest.notifyMoveFinished(npc);
				}
			}
		}

		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if(getIntention() == AI_INTENTION_MOVE_TO)
		{
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ArrivedRevalidate.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtArrivedBlocked(Location blockedAtPos)
	{
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if(getIntention() == AI_INTENTION_MOVE_TO || getIntention() == AI_INTENTION_CAST)
		{
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(blockedAtPos);

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ForgetObject.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the object was targeted  and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to attack, stop the auto-attack, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to cast, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to follow, stop the movement, cancel AI Follow Task and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the targeted object was the actor , cancel AI target, stop AI Follow Task, stop the movement and set the Intention to AI_INTENTION_IDLE </li><BR><BR>
	 */
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		// If the object was targeted  and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE
		if(getTarget() == object)
		{
			setTarget(null);

			if(getIntention() == AI_INTENTION_INTERACT || getIntention() == AI_INTENTION_PICK_UP)
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
		}

		// Check if the object was targeted to attack
		if(getAttackTarget() == object)
		{
			// Cancel attack target
			setAttackTarget(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to cast
		if(getCastTarget() == object)
		{
			// Cancel cast target
			setCastTarget(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to follow
		if(getFollowTarget() == object)
		{
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Stop an AI Follow Task
			stopFollow();

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the targeted object was the actor
		if(_actor == object)
		{
			// Cancel AI target
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);

			// Stop an AI Follow Task
			stopFollow();

			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_IDLE
			changeIntention(AI_INTENTION_IDLE, null, null);
		}
	}

	/**
	 * Launch actions corresponding to the Event Cancel.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();

		// Stop an AI Follow Task
		stopFollow();

		if(!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		}

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event Dead.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)</li><BR><BR>
	 */
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Tasks
		stopAITask();

		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();

		if(!(_actor instanceof L2Playable))
		{
			_actor.setWalking();
		}
	}

	/**
	 * Launch actions corresponding to the Event Fake Death.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 */
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();

		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Init AI
		_intention = AI_INTENTION_IDLE;
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// do nothing
	}

	protected boolean maybeMoveToPosition(Point3D worldPosition, int offset)
	{
		if(worldPosition == null)
		{
			_log.log(Level.WARN, "maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}

		if(offset < 0)
		{
			return false; // skill radius -1
		}

		if(!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().getCollisionRadius(_actor), false))
		{
			if(_actor.isMovementDisabled())
			{
				return true;
			}

			if(!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}

			stopFollow();

			Location loc = null;
			int x = _actor.getX();
			int y = _actor.getY();

			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;

			double dist = Math.sqrt(dx * dx + dy * dy);

			double sin = dy / dist;
			double cos = dx / dist;

			dist -= offset - 5;

			x += (int) (dist * cos);
			y += (int) (dist * sin);
			loc = new Location(x, y, worldPosition.getZ());

			moveTo(loc);
			return true;
		}

		if(getFollowTarget() != null)
		{
			stopFollow();
		}

		return false;
	}

	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the distance between the current position of the L2Character and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, L2SummonAI</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 * @param offset The Interact area radius
	 * @return True if a movement must be done
	 */
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		// Get the distance between the current position of the L2Character and the target (x,y)
		if(target == null)
		{
			_log.log(Level.WARN, "maybeMoveToPawn: target == NULL!");
			return false;
		}
		if(offset < 0)
		{
			return false; // skill radius -1
		}

		offset += _actor.getTemplate().getCollisionRadius(_actor);
		if(target instanceof L2Character)
		{
			offset += ((L2Character) target).getTemplate().getCollisionRadius(_actor);
		}

		if(!_actor.isInsideRadius(target, offset, false, false))
		{
			// Caller should be L2Playable and thinkAttack/thinkCast/thinkInteract/thinkPickUp
			if(getFollowTarget() != null)
			{
				// allow larger hit range when the target is moving (check is run only once per second)
				if(!_actor.isInsideRadius(target, offset + 75, false, false))
				{
					return true;
				}
				stopFollow();
				return false;
			}

			if(_actor.isMovementDisabled())
			{
				// If player is trying attack target but he cannot move to attack target
				// change his intention to idle
				// if (_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				// 	_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

				return true;
			}

			// while flying there is no move to cast
			if(_actor.getAI().getIntention() == AI_INTENTION_CAST && _actor instanceof L2PcInstance && _actor.isTransformed())
			{
				if(!((L2PcInstance) _actor).getTransformation().canStartFollowToCast())
				{
					_actor.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					_actor.sendActionFailed();
					return true;
				}
			}

			// If not running, set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
			if(!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}

			stopFollow();
			if(target instanceof L2Character && !(target instanceof L2DoorInstance))
			{
				if(((L2Character) target).isMoving())
				{
					offset -= 70;
				}
				if(offset < 5)
				{
					offset = 5;
				}

				startFollow((L2Character) target, offset);
			}
			else
			{
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}
			return true;
		}

		if(getFollowTarget() != null)
		{
			stopFollow();
		}

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		// clientStopMoving(null);
		return false;
	}

	/**
	 * Modify current Intention and actions if the target is lost or dead.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : <I>If the target is lost or dead</I></B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, L2SummonAI</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 * @return True if the target is lost or dead (false if fakedeath)
	 */
	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if(target == null || target.isAlikeDead())
		{
			//check if player is fakedeath
			if(target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
				return false;
			}

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}

	/**
	 * Modify current Intention and actions if the target is lost.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> : <I>If the target is lost</I></B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, L2SummonAI</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 * @return True if the target is lost
	 */
	protected boolean checkTargetLost(L2Object target)
	{
		// check if player is fakedeath
		if(target instanceof L2PcInstance)
		{
			L2PcInstance target2 = (L2PcInstance) target; //convert object to chara

			if(target2.isFakeDeath())
			{
				target2.stopFakeDeath(true);
				return false;
			}
		}
		if(target == null)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		if(_actor != null && _skill != null && _skill.isOffensive() && _skill.getSkillRadius() > 0 && Config.GEODATA_ENABLED && !GeoEngine.getInstance().canSeeTarget(_actor, target))
		{
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}

	public boolean canAura(L2Skill sk)
	{
		if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA || sk.getTargetType() == L2TargetType.TARGET_AURA_CORPSE_MOB)
		{
			for(L2Object target : _actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()))
			{
				if(target.equals(getAttackTarget()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean canAOE(L2Skill sk)
	{
		if(sk.getSkillType() != L2SkillType.NEGATE || sk.getSkillType() != L2SkillType.CANCEL)
		{
			if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA || sk.getTargetType() == L2TargetType.TARGET_AURA_CORPSE_MOB)
			{
				boolean cancast = true;
				for(L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()))
				{
					if(!GeoEngine.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}

					if(target instanceof L2Attackable)
					{
						L2Npc targets = (L2Npc) target;
						L2Npc actors = (L2Npc) _actor;

						if(targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) || actors.getClan() == null && actors.getIsChaos() == 0)
						{
							continue;
						}
					}
					for(L2Effect effect : target.getAllEffects())
					{
						try
						{
							if(effect != null && effect.getSkill().equals(sk))
							{
								cancast = false;
								break;
							}
						}
						catch(Exception e)
						{
							_log.log(Level.ERROR, "canAOE: Effect " + effect);
						}
					}
				}
				if(cancast)
				{
					return true;
				}
			}
			else if(sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA)
			{
				boolean cancast = true;

				if(getAttackTarget() != null)
				{
					for(L2Character target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()))
					{
						if(!GeoEngine.getInstance().canSeeTarget(_actor, target) || target == null)
						{
							continue;
						}
						if(target instanceof L2Attackable)
						{
							L2Npc targets = (L2Npc) target;
							L2Npc actors = (L2Npc) _actor;
							if(targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) || actors.getClan() == null && actors.getIsChaos() == 0)
							{
								continue;
							}
						}
						L2Effect[] effects = target.getAllEffects();
						if(effects.length > 0)
						{
							cancast = true;
						}
					}
				}

				if(cancast)
				{
					return true;
				}
			}
		}
		else
		{
			if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA || sk.getTargetType() == L2TargetType.TARGET_AURA_CORPSE_MOB)
			{
				boolean cancast = false;
				for(L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()))
				{
					if(!GeoEngine.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if(target instanceof L2Attackable)
					{
						L2Npc targets = (L2Npc) target;
						L2Npc actors = (L2Npc) _actor;
						if(targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) || actors.getClan() == null && actors.getIsChaos() == 0)
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					if(effects.length > 0)
					{
						cancast = true;
					}
				}
				if(cancast)
				{
					return true;
				}
			}
			else if(sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA)
			{
				boolean cancast = true;
				for(L2Character target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()))
				{
					if(!GeoEngine.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if(target instanceof L2Attackable)
					{
						L2Npc targets = (L2Npc) target;
						L2Npc actors = (L2Npc) _actor;
						if(targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) || actors.getClan() == null && actors.getIsChaos() == 0)
						{
							continue;
						}
					}
					for(L2Effect effect : target.getAllEffects())
					{
						if(effect != null && effect.getSkill().equals(sk))
						{
							cancast = false;
							break;
						}
					}

				}
				if(cancast)
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean canParty(L2Skill sk)
	{
		if(sk.getTargetType() == L2TargetType.TARGET_PARTY)
		{
			int count = 0;
			int ccount = 0;
			for(L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius()))
			{
				if(!(target instanceof L2Attackable) || !GeoEngine.getInstance().canSeeTarget(_actor, target))
				{
					continue;
				}
				L2Npc targets = (L2Npc) target;
				L2Npc actors = (L2Npc) _actor;
				if(actors.getFactionId() != null && targets.getFactionId().equals(actors.getFactionId()))
				{
					count++;
					for(L2Effect effect : target.getAllEffects())
					{
						if(effect != null && effect.getSkill().equals(sk))
						{
							ccount++;
							break;
						}
					}
				}
			}
			if(ccount < count)
			{
				return true;
			}

		}
		return false;
	}

	public boolean isParty(L2Skill sk)
	{
		return sk.getTargetType() == L2TargetType.TARGET_PARTY;
	}

	public static class IntentionCommand
	{
		protected final CtrlIntention _crtlIntention;
		protected final Object _arg0;
		protected final Object _arg1;

		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}

		public CtrlIntention getCtrlIntention()
		{
			return _crtlIntention;
		}
	}

	protected class SelfAnalysis
	{
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isHealer;
		public boolean isFighter;
		public boolean cannotMoveOnLand;
		public List<L2Skill> generalSkills = new FastList<>();
		public List<L2Skill> buffSkills = new FastList<>();
		public int lastBuffTick;
		public List<L2Skill> debuffSkills = new FastList<>();
		public int lastDebuffTick;
		public List<L2Skill> cancelSkills = new FastList<>();
		public List<L2Skill> healSkills = new FastList<>();
		//public List<L2Skill> trickSkills = new FastList<L2Skill>();
		public List<L2Skill> generalDisablers = new FastList<>();
		public List<L2Skill> sleepSkills = new FastList<>();
		public List<L2Skill> rootSkills = new FastList<>();
		public List<L2Skill> muteSkills = new FastList<>();
		public List<L2Skill> resurrectSkills = new FastList<>();
		public boolean hasHealOrResurrect;
		public boolean hasLongRangeSkills;
		public boolean hasLongRangeDamageSkills;
		public int maxCastRange;

		public SelfAnalysis()
		{
		}

		public void init()
		{
			switch(((L2NpcTemplate) _actor.getTemplate()).getAiType())
			{
				case FIGHTER:
					isFighter = true;
					break;
				case MAGE:
					isMage = true;
					break;
				case CORPSE:
				case BALANCED:
					isBalanced = true;
					break;
				case ARCHER:
					isArcher = true;
					break;
				case HEALER:
					isHealer = true;
					break;
				default:
					isFighter = true;
					break;
			}
			// water movement analysis
			if(_actor instanceof L2Npc)
			{
				int npcId = ((L2Npc) _actor).getNpcId();

				switch(npcId)
				{
					case 20314: // great white shark
					case 20849: // Light Worm
						cannotMoveOnLand = true;
						break;
					default:
						cannotMoveOnLand = false;
						break;
				}
			}
			// skill analysis
			for(L2Skill sk : _actor.getAllSkills())
			{
				if(sk.isPassive())
				{
					continue;
				}
				int castRange = sk.getCastRange();
				boolean hasLongRangeDamageSkill = false;
				switch(sk.getSkillType())
				{
					case HEAL:
					case HEAL_PERCENT:
					case HEAL_STATIC:
					case BALANCE_LIFE:
					case HOT:
						healSkills.add(sk);
						hasHealOrResurrect = true;
						continue; // won't be considered something for fighting
					case BUFF:
						buffSkills.add(sk);
						continue; // won't be considered something for fighting
					case PARALYZE:
					case FLY_UP:
					case STUN:
					case KNOCK_DOWN:
					case KNOCK_BACK:
						// hardcoding petrification until improvements are made to
						// EffectTemplate... petrification is totally different for
						// AI than paralyze
						switch(sk.getId())
						{
							case 367:
							case 4111:
							case 4383:
							case 4616:
							case 4578:
								sleepSkills.add(sk);
								break;
							default:
								generalDisablers.add(sk);
								break;
						}
						break;
					case MUTE:
						muteSkills.add(sk);
						break;
					case SLEEP:
						sleepSkills.add(sk);
						break;
					case ROOT:
						rootSkills.add(sk);
						break;
					case FEAR: // could be used as an alternative for healing?
					case CONFUSION:
						//  trickSkills.add(sk);
					case DEBUFF:
						debuffSkills.add(sk);
						break;
					case CANCEL:
					case NEGATE:
						cancelSkills.add(sk);
						break;
					case RESURRECT:
						resurrectSkills.add(sk);
						hasHealOrResurrect = true;
						break;
					case NOTDONE:
					case COREDONE:
						continue; // won't be considered something for fighting
					default:
						generalSkills.add(sk);
						hasLongRangeDamageSkill = true;
						break;
				}
				if(castRange > 70)
				{
					hasLongRangeSkills = true;
					if(hasLongRangeDamageSkill)
					{
						hasLongRangeDamageSkills = true;
					}
				}
				if(castRange > maxCastRange)
				{
					maxCastRange = castRange;
				}

			}
			// Because of missing skills, some mages/balanced cannot play like mages
			if(!hasLongRangeDamageSkills && isMage)
			{
				isBalanced = true;
				isMage = false;
				isFighter = false;
			}
			if(!hasLongRangeSkills && (isMage || isBalanced))
			{
				isBalanced = false;
				isMage = false;
				isFighter = true;
			}
			if(generalSkills.isEmpty() && isMage)
			{
				isBalanced = true;
				isMage = false;
			}
		}
	}

	protected class TargetAnalysis
	{
		public L2Character character;
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isFighter;
		public boolean isCanceled;
		public boolean isSlower;
		public boolean isMagicResistant;

		public TargetAnalysis()
		{
		}

		public void update(L2Character target)
		{
			// update status once in 4 seconds
			if(target.equals(character) && Rnd.getChance(25))
			{
				return;
			}
			character = target;
			if(target == null)
			{
				return;
			}
			isMage = false;
			isBalanced = false;
			isArcher = false;
			isFighter = false;
			isCanceled = false;

			if(target.getMAtk(null, null) > 1.5 * target.getPAtk(null))
			{
				isMage = true;
			}
			else if(target.getPAtk(null) * 0.8 < target.getMAtk(null, null) || target.getMAtk(null, null) * 0.8 > target.getPAtk(null))
			{
				isBalanced = true;
			}
			else
			{
				L2Weapon weapon = target.getActiveWeaponItem();
				if(weapon != null && (weapon.getItemType() == L2WeaponType.BOW || weapon.getItemType() == L2WeaponType.CROSSBOW || weapon.getItemType() == L2WeaponType.TWOHANDCROSSBOW))
				{
					isArcher = true;
				}
				else
				{
					isFighter = true;
				}
			}
			isSlower = target.getRunSpeed() < _actor.getRunSpeed() - 3;
			isMagicResistant = target.getMDef(null, null) * 1.2 > _actor.getMAtk(null, null);
			if(target.getBuffCount() < 4)
			{
				isCanceled = true;
			}
		}
	}
}
