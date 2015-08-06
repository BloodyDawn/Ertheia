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
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.LocationsTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2FriendlyMobInstance;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.model.actor.instance.L2TrainingDollInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.actor.templates.L2NpcTemplate.AIType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_IDLE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_MOVE_TO;

/**
 * This class manages AI of L2Attackable.<BR>
 * <BR>
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	private static final int RANDOM_WALK_RATE = 30; // confirmed
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 1200; // int ticks, i.e. 2min
	private final L2NpcTemplate _skillrender;
	int lastBuffTick;
	/**
	 * The L2Attackable AI task executed every 1s (call onEvtThink method)
	 */
	private Future<?> _aiTask;
	/**
	 * The delay after which the attacked is stopped
	 */
	private int _attackTimeout;
	/**
	 * The L2Attackable aggro counter
	 */
	private int _globalAggro;
	/**
	 * The flag used to indicate that a thinking action is in progress
	 */
	private boolean _thinking; // to prevent recursive thinking
	private int timepass;
	private int chaostime;
	private List<L2Skill> shortRangeSkills = new ArrayList<>();
	private List<L2Skill> longRangeSkills = new ArrayList<>();

	/**
	 * Constructor of L2AttackableAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 *
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		_skillrender = NpcTable.getInstance().getTemplate(getActiveChar().getTemplate().getNpcId());
		// _selfAnalysis.init();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}

	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li><BR><BR>
	 *
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li><BR><BR>
	 *
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
	 *
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 * @return True if the target is autoattackable (depends on the actor type).
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		if(target == null || getActiveChar() == null)
		{
			return false;
		}

		L2Attackable me = getActiveChar();

		// Check if the target isn't invulnerable
		if(target.isInvul())
		{
			// However Invincible requires to check GMs specially
			if(target instanceof L2PcInstance && target.isGM())
			{
				return false;
			}
			if(target instanceof L2Summon && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}

		// Check if the target isn't a Folk or a Door
		if(target instanceof L2DoorInstance)
		{
			return false;
		}

		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if(target.isAlikeDead() || target instanceof L2Playable && !me.isInsideRadius(target, me.getAggroRange(), true, false))
		{
			return false;
		}

		// Check if the target is a L2PlayableInstance
		if(target instanceof L2Playable)
		{
			// Check if the AI isn't a Raid Boss, can See Silent Moving players
			// and the target isn't in silent move mode
			if(!me.isRaid() && !me.canSeeThroughSilentMove() && ((L2Playable) target).isSilentMoving())
			{
				return false;
			}
		}

		// Check if the actor is a L2GuardInstance
		if(getActiveChar() instanceof L2GuardInstance)
		{
			if(target instanceof L2PcInstance && ((L2PcInstance) target).hasBadReputation())
			{
				return GeoEngine.getInstance().canSeeTarget(me, target);
			}

			// Для гвардов бьющих тренировчных кукол
			if(target instanceof L2TrainingDollInstance)
			{
				return GeoEngine.getInstance().canSeeTarget(me, target);
			}

			// Check if the L2MonsterInstance target is aggressive
			if(target instanceof L2MonsterInstance && Config.GUARD_ATTACK_AGGRO_MOB)
			{
				return ((L2MonsterInstance) target).isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target);
			}

			return false;
		}

		// Check if the target is a L2PcInstance
		if(target instanceof L2PcInstance)
		{
			L2PcInstance player = target.getActingPlayer();
			// Don't take the aggro if the GM has the access level below or
			// equal to GM_DONT_TAKE_AGGRO
			if(player.isGM() && !player.getAccessLevel().canTakeAggro())
			{
				return false;
			}

			// TODO: Ideally, autoattack condition should be called from the AI
			// script. In that case,
			// it should only implement the basic behaviors while the script
			// will add more specific
			// behaviors (like varka/ketra alliance, etc). Once implemented,
			// remove specialized stuff
			// from this location. (Fulminus)

			// Check if player is an ally (comparing mem addr)
			if(me.getFactionId() != null)
			{
				switch(me.getFactionId())
				{
					case "varka_silenos_clan":
						if(player.isAlliedWithVarka())
						{
							return false;
						}
					case "ketra_orc_clan":
						if(player.isAlliedWithKetra())
						{
							return false;
						}
					case "anti_mob_guard": // anti_mob_guard используется для гвардов, сражающихся с мобами
						return false;
				}
			}

			// check if the target is within the grace period for JUST getting
			// up from fake death
			if(player.isRecentFakeDeath())
			{
				return false;
			}

			// if (_selfAnalysis.cannotMoveOnLand &&
			// !target.isInsideZone(L2Character.ZONE_WATER))
			// return false;
		}

		// Check if the target is a L2Summon
		if(target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if(owner != null)
			{
				// Don't take the aggro if the GM has the access level below or
				// equal to GM_DONT_TAKE_AGGRO
				if(owner.isGM() && (owner.isInvul() || !owner.getAccessLevel().canTakeAggro()))
				{
					return false;
				}
				// Check if player is an ally (comparing mem addr)
				if(me.getFactionId() != null)
				{
					switch(me.getFactionId())
					{
						case "varka_silenos_clan":
							if(owner.isAlliedWithVarka())
							{
								return false;
							}
						case "ketra_orc_clan":
							if(owner.isAlliedWithKetra())
							{
								return false;
							}
						case "anti_mob_guard": // anti_mob_guard используется для гвардов, сражающихся с мобами
							return false;
					}
				}
			}
		}

		if(getActiveChar() instanceof L2FriendlyMobInstance) // the actor is a L2FriendlyMobInstance
		{
			if(target instanceof L2FriendlyMobInstance) // Для бьющихся гвардов, нельзя атаковать Friendly мобов
			{
				return false;
			}
			if(target instanceof L2Attackable)
			{
				if(getActiveChar().getEnemyClan() != null && ((L2Attackable) target).getClan() != null)
				{
					if(getActiveChar().getEnemyClan().equals(((L2Attackable) target).getClan()))
					{
						if(getActiveChar().isInsideRadius(target, getActiveChar().getEnemyRange(), false, false))
						{
							return GeoEngine.getInstance().canSeeTarget(getActiveChar(), target);
						}
						return false;
					}
				}
			}
			else if(target instanceof L2Npc) // Check if the target isn't another L2Npc
			{
				return false;
			}
			else if(target instanceof L2PcInstance && ((L2PcInstance) target).hasBadReputation())
			{
				return GeoEngine.getInstance().canSeeTarget(me, target); // Los Check
			}
			return false;
		}
		else
		{
			if(target instanceof L2Attackable)
			{
				if(getActiveChar().getEnemyClan() == null || ((L2Attackable) target).getClan() == null)
				{
					return false;
				}

				if(!target.isAutoAttackable(getActiveChar()))
				{
					return false;
				}

				if(getActiveChar().getEnemyClan().equals(((L2Attackable) target).getClan()))
				{
					if(getActiveChar().isInsideRadius(target, getActiveChar().getEnemyRange(), false, false))
					{
						return GeoEngine.getInstance().canSeeTarget(getActiveChar(), target);
					}
					return false;
				}
				if(getActiveChar().getIsChaos() > 0 && me.isInsideRadius(target, getActiveChar().getIsChaos(), false, false))
				{
					if(getActiveChar().getFactionId() != null && getActiveChar().getFactionId().equals(((L2Attackable) target).getFactionId()))
					{
						return false;
					}
					// Los Check
					return GeoEngine.getInstance().canSeeTarget(me, target);
				}
			}

			if(target instanceof L2Attackable || target instanceof L2Npc)
			{
				return false;
			}

			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if(!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(L2Character.ZONE_PEACE))
			{
				return false;
			}

			if(me.isChampion() && Config.CHAMPION_PASSIVE)
			{
				return false;
			}

			// Check if the actor is Aggressive
			return me.isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target);
		}
	}

	public void startAITask()
	{
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if(_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}

	/**
	 * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 *
	 */
	@Override
	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		synchronized(this)
		{
			if(intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
			{
				// Check if actor is not dead
				L2Attackable npc = getActiveChar();
				if(!npc.isAlikeDead())
				{
					// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
					if(npc.getKnownList().getKnownPlayers().isEmpty())
					{
						if(npc.getSpawn() != null)
						{
							int range = Config.MAX_DRIFT_RANGE;
							if(!npc.isInsideRadius(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), range + range, true, false))
							{
								intention = AI_INTENTION_ACTIVE;
							}
						}
					}
					else
					{
						intention = AI_INTENTION_ACTIVE;
					}
				}

				if(intention == AI_INTENTION_IDLE)
				{
					// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
					super.changeIntention(AI_INTENTION_IDLE, null, null);

					// Stop AI task and detach AI from NPC
					if(_aiTask != null)
					{
						_aiTask.cancel(true);
						_aiTask = null;
					}

					// Cancel the AI
					_accessor.detachAI();

					return;
				}
			}

			// Set the Intention of this L2AttackableAI to intention
			super.changeIntention(intention, arg0, arg1);

			// If not idle - create an AI task (schedule onEvtThink repeatedly)
			startAITask();
		}
	}

	@Override
	public void stopAITask()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}

	private void thinkCast()
	{
		if(checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		if(maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		clientStopMoving(null);
		setIntention(AI_INTENTION_ACTIVE);
		_accessor.doCast(_skill);
	}

	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li> <li>
	 * If the actor is Aggressive and can attack, add all autoAttackable
	 * L2Character in its Aggro Range to its _aggroList, chose a target and
	 * order to attack it</li> <li>If the actor is a L2GuardInstance that can't
	 * attack, order to it to return to its home location</li> <li>If the actor
	 * is a L2MonsterInstance that can't attack, order to it to random walk
	 * (1/100)</li><BR>
	 * <BR>
	 */
	private void thinkActive()
	{
		L2Attackable npc = getActiveChar();

		// Update every 1s the _globalAggro counter to come close to 0
		if(_globalAggro != 0)
		{
			if(_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}

		//Mobs should not be aggresive behind some spawn radius
		boolean inAggRadius = true;
		if(npc != null && npc.getSpawn() != null)
		{
			inAggRadius = npc.isInsideRadius(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), (int) (npc.getAggroRange() * 1.5), true, true);
		}

		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its
		// _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because
		// _globalAggro is set to -10
		if(_globalAggro >= 0 && inAggRadius)
		{
			// Get all visible objects inside its Aggro Range
			for(L2Object obj : npc.getKnownList().getKnownObjects().values())
			{
				if(!(obj instanceof L2Character || obj instanceof L2StaticObjectInstance))
				{
					continue;
				}

				L2Character target = (L2Character) obj;

				if(target instanceof L2PcInstance && ((L2PcInstance) target).isSpawnProtected())
				{
					continue;
				}

				// TODO: The AI Script ought to handle aggro behaviors in
				// onSee. Once implemented, aggro behaviors ought
				// to be removed from here. (Fulminus)
				// For each L2Character check if the target is
				// autoattackable
				if(autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this
					// L2Character target contained in _aggroList
					int hating = npc.getHating(target);

					// Add the attacker to the L2Attackable _aggroList with
					// 0 damage and 1 hate
					if(hating == 0)
					{
						npc.addDamageHate(target, 0, 0);
					}
				}
			}

			// Chose a target from its aggroList
			L2Character hated;
			hated = npc.isConfused() ? getAttackTarget() : npc.getMostHated();

			// Order to the L2Attackable to attack the target
			if(hated != null && !npc.isCoreAIDisabled())
			{
				// Get the hate level of the L2Attackable against this
				// L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);

				if(aggro + _globalAggro > 0)
				{
					// Set the L2Character movement type to run and send
					// Server->Client packet ChangeMoveType to all others
					// L2PcInstance
					if(!npc.isRunning())
					{
						npc.setRunning();
					}

					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(AI_INTENTION_ATTACK, hated);
				}
				return;
			}
		}

		// Chance to forget attackers after some time
		if(npc.getCurrentHp() == npc.getMaxHp() && npc.getCurrentMp() == npc.getMaxMp() && !npc.getAttackByList().isEmpty() && Rnd.get(500) == 0)
		{
			npc.clearAggroList();
			npc.getAttackByList().clear();
			if(npc instanceof L2MonsterInstance)
			{
				if(((L2MonsterInstance) npc).hasMinions())
				{
					((L2MonsterInstance) npc).getMinionList().deleteReusedMinions();
				}
			}
		}

		// Check if the actor is a L2GuardInstance
		if(npc instanceof L2GuardInstance)
		{
			// Order to the L2GuardInstance to return to its home location
			// because there's no target to attack
			npc.returnHome();
		}

		// Check if the mob should not return to spawn point
		if(!npc.canReturnToSpawnPoint())
		{
			return;
		}

		// Minions following leader
		L2Character leader = npc.getLeader();
		if(leader != null && !leader.isAlikeDead())
		{
			int offset;
			int minRadius = 30;
			offset = npc.isRaidMinion() ? 500 : 200;

			if(leader.isRunning())
			{
				npc.setRunning();
			}
			else
			{
				npc.setWalking();
			}

			if(npc.getPlanDistanceSq(leader) > offset * offset)
			{
				int x1;
				int y1;
				int z1;
				x1 = Rnd.get(minRadius << 1, offset << 1); // x
				y1 = Rnd.get(x1, offset << 1); // distance
				y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
				x1 = x1 > offset + minRadius ? leader.getX() + x1 - offset : leader.getX() - x1 + minRadius;
				y1 = y1 > offset + minRadius ? leader.getY() + y1 - offset : leader.getY() - y1 + minRadius;

				z1 = leader.getZ();
				// Move the actor to Location (x,y,z) server side AND client
				// side by sending Server->Client packet CharMoveToLocation
				// (broadcast)
				moveTo(new Location(x1, y1, z1));
			}
			else if(Rnd.get(RANDOM_WALK_RATE) == 0)
			{
				for(L2Skill sk : _skillrender.getBuffSkills())
				{
					if(cast(sk))
					{
						return;
					}
				}
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if(npc.getSpawn() != null && Rnd.get(RANDOM_WALK_RATE) == 0 && !npc.isNoRndWalk())
		{
			int x1;
			int y1;
			int z1;
			int range = Config.MAX_DRIFT_RANGE;

			for(L2Skill sk : _skillrender.getBuffSkills())
			{
				if(cast(sk))
				{
					return;
				}
			}

			// If NPC with random coord in territory
			if(npc.getSpawn().getLocx() == 0 && npc.getSpawn().getLocy() == 0)
			{
				// Calculate a destination point in the spawn area
				int[] p = LocationsTable.getInstance().getRandomPoint(npc.getSpawn().getLocationId());
				x1 = p[0];
				y1 = p[1];
				z1 = p[2];

				// Calculate the distance between the current position of the
				// L2Character and the target (x,y)
				double distance2 = npc.getPlanDistanceSq(x1, y1);

				if(distance2 > (range + range) * (range + range))
				{
					npc.setisReturningToSpawnPoint(true);
					float delay = (float) Math.sqrt(distance2) / range;
					x1 = npc.getX() + (int) ((x1 - npc.getX()) / delay);
					y1 = npc.getY() + (int) ((y1 - npc.getY()) / delay);
				}

				// If NPC with random fixed coord, don't move (unless needs to return to spawnpoint)
				if(LocationsTable.getInstance().getProcMax(npc.getSpawn().getLocationId()) > 0 && !npc.isReturningToSpawnPoint())
				{
					return;
				}
			}
			else
			{
				// If NPC with fixed coord
				x1 = npc.getSpawn().getLocx();
				y1 = npc.getSpawn().getLocy();
				z1 = npc.getSpawn().getLocz();

				if(npc.isInsideRadius(x1, y1, range, false))
				{
					x1 = Rnd.get(range << 1); // x
					y1 = Rnd.get(x1, range << 1); // distance
					y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
					x1 += npc.getSpawn().getLocx() - range;
					y1 += npc.getSpawn().getLocy() - range;
					z1 = npc.getZ();
				}
				else
				{
					npc.setisReturningToSpawnPoint(true);
				}
			}

			// _log.log(Level.DEBUG, "Current pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by
			// sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(new Location(x1, y1, z1));
		}
	}

	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update the attack timeout if actor is running</li> <li>If target is
	 * dead or timeout is expired, stop this attack and set the Intention to
	 * AI_INTENTION_ACTIVE</li> <li>Call all L2Object of its Faction inside the
	 * Faction Range</li> <li>Chose a target and order to attack it with magic
	 * skill or physical attack</li><BR>
	 * <BR>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private void thinkAttack()
	{
		L2Attackable npc = getActiveChar();
		if(npc.isCastingNow())
		{
			return;
		}

		L2Character originalAttackTarget = getAttackTarget();
		// Check if target is dead or if timeout is expired to stop this attack
		if(originalAttackTarget == null || originalAttackTarget.isAlikeDead() || _attackTimeout < GameTimeController.getInstance().getGameTicks())
		{
			// Stop hating this target after the attack timeout or if target is
			// dead
			if(originalAttackTarget != null)
			{
				npc.stopHating(originalAttackTarget);
			}

			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);

			npc.setWalking();
			return;
		}

		int collision = npc.getTemplate().getCollisionRadius(npc);

		// Handle all L2Object of its Faction inside the Faction Range

		String faction_id = getActiveChar().getFactionId();
		if(faction_id != null && !faction_id.isEmpty())
		{
			int factionRange = npc.getClanRange() + collision;

			// Go through all L2Object that belong to its faction
			try
			{
				// Check if the L2Object is inside the Faction Range of
				// the actor
				// && GeoEngine.getInstance().canSeeTarget(called,
				// npc))
				npc.getKnownList().getKnownObjects().values().stream().filter(obj -> obj instanceof L2Npc).forEach(obj -> {
					L2Npc called = (L2Npc) obj;

					// Check if the L2Object is inside the Faction Range of
					// the actor
					if(npc.isInsideRadius(called, factionRange, true, false) && called.hasAI())
					{
						if(Math.abs(originalAttackTarget.getZ() - called.getZ()) < 600 && npc.getAttackByList().contains(originalAttackTarget) && (called.getAI()._intention == AI_INTENTION_IDLE || called.getAI()._intention == AI_INTENTION_ACTIVE) && called.getInstanceId() == npc.getInstanceId())
						// && GeoEngine.getInstance().canSeeTarget(called,
						// npc))
						{
							if(originalAttackTarget.isPlayable())
							{
								List<Quest> quests = called.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL);
								if(quests != null && !quests.isEmpty())
								{
									L2PcInstance player = originalAttackTarget.getActingPlayer();
									boolean isSummon = originalAttackTarget.isSummon();
									for(Quest quest : quests)
									{
										quest.notifyFactionCall(called, getActiveChar(), player, isSummon);
									}
								}
							}
							else if(called instanceof L2Attackable && getAttackTarget() != null && called.getAI()._intention != AI_INTENTION_ATTACK)
							{
								((L2Attackable) called).addDamageHate(getAttackTarget(), 0, npc.getHating(getAttackTarget()));
								called.getAI().setIntention(AI_INTENTION_ATTACK, getAttackTarget());
							}
						}
					}
				});
			}
			catch(NullPointerException e)
			{
				_log.log(Level.ERROR, "L2AttackableAI: thinkAttack() faction call failed: " + e.getMessage(), e);
			}
		}

		if(npc.isCoreAIDisabled())
		{
			return;
		}

        /*
           * if(_actor.getTarget() == null || this.getAttackTarget() == null ||
           * this.getAttackTarget().isDead() || ctarget == _actor)
           * AggroReconsider();
           */

		// ----------------------------------------------------------------

		// ------------------------------------------------------------------------------
		// Initialize data
		L2Character mostHate = npc.getMostHated();
		if(mostHate == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		setAttackTarget(mostHate);
		npc.setTarget(mostHate);

		int combinedCollision = collision + mostHate.getTemplate().getCollisionRadius(mostHate);

		if(!_skillrender.getSuicideSkills().isEmpty() && (int) (npc.getCurrentHp() / npc.getMaxHp() * 100) < 30)
		{
			L2Skill skill = _skillrender.getSuicideSkills().get(Rnd.get(_skillrender.getSuicideSkills().size()));
			if(Util.checkIfInRange(skill.getSkillRadius(), getActiveChar(), mostHate, false) && Rnd.get(100) < Rnd.get(npc.getMinSkillChance(), npc.getMaxSkillChance()))
			{
				if(cast(skill))
				{
					return;
				}

				for(L2Skill sk : _skillrender.getSuicideSkills())
				{
					if(cast(sk))
					{
						return;
					}
				}
			}
		}

		// ------------------------------------------------------
		// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		// Note from Gnacik:
		// On l2god because of that sometimes mobs don't attack player only
		// running
		// around player without any sense, so decrease chance for now
		if(!npc.isMovementDisabled() && Rnd.getChance(3))
		{
			Location loc = null;
			for(L2Object nearby : npc.getKnownList().getKnownObjects().values())
			{
				if(nearby instanceof L2Attackable && npc.isInsideRadius(nearby, collision, false, false) && !nearby.equals(mostHate))
				{
					int newX = combinedCollision + Rnd.get(40);
					newX = Rnd.nextBoolean() ? mostHate.getX() + newX : mostHate.getX() - newX;
					int newY = combinedCollision + Rnd.get(40);
					newY = Rnd.nextBoolean() ? mostHate.getY() + newY : mostHate.getY() - newY;

					if(!npc.isInsideRadius(newX, newY, collision, false))
					{
						int newZ = npc.getZ() + 30;
						if(!Config.GEODATA_ENABLED || GeoEngine.getInstance().canMoveFromToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getInstanceId()))
						{
							loc = new Location(newX, newY, newZ);
							moveTo(loc);
						}
					}
					return;
				}
			}
		}
		// Dodge if its needed
		if(!npc.isMovementDisabled() && npc.getDodgeChance() > 0)
		{
			if(Rnd.getChance(npc.getDodgeChance()))
			{
				// Micht: kepping this one otherwise we should do 2 sqrt
				double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
				if(Math.sqrt(distance2) <= 60 + combinedCollision)
				{
					int posX = npc.getX();
					int posY = npc.getY();
					int posZ = npc.getZ() + 30;

					posX += originalAttackTarget.getX() < posX ? 300 : 300;

					posY += originalAttackTarget.getY() < posY ? 300 : 300;

					if(!Config.GEODATA_ENABLED || GeoEngine.getInstance().canMoveFromToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, npc.getInstanceId()))
					{
						setIntention(AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ, 0));
					}
					return;
				}
			}
		}

		// ------------------------------------------------------------------------------
		// BOSS/Raid Minion Target Reconsider
		if(npc.isRaid() || npc.isRaidMinion())
		{
			chaostime++;
			if(npc instanceof L2RaidBossInstance)
			{
				if(((L2MonsterInstance) npc).hasMinions())
				{
					if(chaostime > Config.RAID_CHAOS_TIME)
					{
						if(Rnd.getChance(100 - npc.getCurrentHp() * 200 / npc.getMaxHp()))
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
				else
				{
					if(chaostime > Config.RAID_CHAOS_TIME)
					{
						if(Rnd.getChance(100 - npc.getCurrentHp() * 100 / npc.getMaxHp()))
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
			}
			else if(npc instanceof L2GrandBossInstance)
			{
				if(chaostime > Config.GRAND_CHAOS_TIME)
				{
					double chaosRate = 100 - npc.getCurrentHp() * 300 / npc.getMaxHp();
					if(chaosRate <= 10 && Rnd.getChance(10) || chaosRate > 10 && Rnd.getChance(chaosRate))
					{
						aggroReconsider();
						chaostime = 0;
						return;
					}
				}
			}
			else
			{
				if(chaostime > Config.MINION_CHAOS_TIME)
				{
					if(Rnd.getChance(100 - npc.getCurrentHp() * 200 / npc.getMaxHp()))
					{
						aggroReconsider();
						chaostime = 0;
						return;
					}
				}
			}
		}

		if(!_skillrender.getGeneralskills().isEmpty())
		{
			// -------------------------------------------------------------------------------
			// Heal Condition
			if(!_skillrender.getHealSkills().isEmpty())
			{
				double percentage = npc.getCurrentHp() / npc.getMaxHp() * 100;
				if(npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if(leader != null && !leader.isDead() && Rnd.getChance(100 - leader.getCurrentHp() / leader.getMaxHp() * 100))
					{
						for(L2Skill sk : _skillrender.getHealSkills())
						{
							if(sk.getTargetType() == L2TargetType.TARGET_SELF)
							{
								continue;
							}
							if(!checkSkillCastConditions(sk))
							{
								continue;
							}

							if(!Util.checkIfInRange(sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius(leader), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius(leader));
								return;
							}
							if(GeoEngine.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								clientStopMoving(null);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if(Rnd.getChance((100 - percentage) / 3))
				{
					for(L2Skill sk : _skillrender.getHealSkills())
					{
						if(!checkSkillCastConditions(sk))
						{
							continue;
						}

						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				for(L2Skill sk : _skillrender.getHealSkills())
				{
					if(!checkSkillCastConditions(sk))
					{
						continue;
					}

					if(sk.getTargetType() == L2TargetType.TARGET_ONE)
					{
						for(L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if(!(obj instanceof L2Attackable) || obj.isDead())
							{
								continue;
							}

							L2Attackable targets = (L2Attackable) obj;
							if(npc.getFactionId() != null && !npc.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
							if(Rnd.getChance((100 - percentage) / 10))
							{
								if(GeoEngine.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
					}
					if(isParty(sk))
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			// -------------------------------------------------------------------------------
			// Res Skill Condition
			if(!_skillrender.getResSkills().isEmpty())
			{
				if(npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if(leader != null && leader.isDead())
					{
						for(L2Skill sk : _skillrender.getResSkills())
						{
							if(sk.getTargetType() == L2TargetType.TARGET_SELF)
							{
								continue;
							}
							if(!checkSkillCastConditions(sk))
							{
								continue;
							}

							if(!Util.checkIfInRange(sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius(leader), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius(leader));
								return;
							}
							if(GeoEngine.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				for(L2Skill sk : _skillrender.getResSkills())
				{
					if(!checkSkillCastConditions(sk))
					{
						continue;
					}

					if(sk.getTargetType() == L2TargetType.TARGET_ONE)
					{
						for(L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if(!(obj instanceof L2Attackable) || !obj.isDead())
							{
								continue;
							}

							L2Attackable targets = (L2Attackable) obj;
							if(npc.getFactionId() != null && !npc.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							if(Rnd.getChance(10))
							{
								if(GeoEngine.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
					}
					if(isParty(sk))
					{
						clientStopMoving(null);
						L2Object target = getAttackTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						return;
					}
				}
			}
		}

		double dist = Math.sqrt(npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY()));
		int dist2 = (int) dist - collision;
		int range = npc.getPhysicalAttackRange() + combinedCollision;
		if(mostHate.isMoving())
		{
			range += 50;
			if(npc.isMoving())
			{
				range += 50;
			}
		}

		// -------------------------------------------------------------------------------
		// Immobilize Condition
		if(npc.isMovementDisabled() && (dist > range || mostHate.isMoving()) || dist > range && mostHate.isMoving())
		{
			movementDisable();
			return;
		}
		timepass = 0;
		// --------------------------------------------------------------------------------
		//Skill Use
		if(!_skillrender.getGeneralskills().isEmpty())
		{
			if(Rnd.getChance(Rnd.get(npc.getMinSkillChance(), npc.getMaxSkillChance())))
			{
				L2Skill skills = _skillrender.getGeneralskills().get(Rnd.get(_skillrender.getGeneralskills().size()));
				if(cast(skills))
				{
					return;
				}
				for(L2Skill sk : _skillrender.getGeneralskills())
				{
					if(cast(sk))
					{
						return;
					}
				}
			}

			// --------------------------------------------------------------------------------
			// Long/Short Range skill usage.
			if(npc.hasLSkill() || npc.hasSSkill())
			{
				List<L2Skill> shortRangeSkills = shortRangeSkillRender();
				if(!shortRangeSkills.isEmpty() && npc.hasSSkill() && dist2 <= 150 && Rnd.getChance(npc.getSSkillChance()))
				{
					L2Skill shortRangeSkill = shortRangeSkills.get(Rnd.get(shortRangeSkills.size()));
					if(shortRangeSkill != null && cast(shortRangeSkill))
					{
						return;
					}
					for(L2Skill sk : shortRangeSkills)
					{
						if(sk != null && cast(sk))
						{
							return;
						}
					}
				}

				List<L2Skill> longRangeSkills = longRangeSkillRender();
				if(!longRangeSkills.isEmpty() && npc.hasLSkill() && dist2 > 150 && Rnd.getChance(npc.getLSkillChance()))
				{
					L2Skill longRangeSkill = longRangeSkills.get(Rnd.get(longRangeSkills.size()));
					if(longRangeSkill != null && cast(longRangeSkill))
					{
						return;
					}
					for(L2Skill sk : longRangeSkills)
					{
						if(sk != null && cast(sk))
						{
							return;
						}
					}
				}
			}
		}

		// --------------------------------------------------------------------------------
		// Starts Melee or Primary Skill
		if(dist2 > range || !GeoEngine.getInstance().canSeeTarget(npc, mostHate))
		{
			if(npc.isMovementDisabled())
			{
				targetReconsider();
			}
			else if(getAttackTarget() != null)
			{
				if(getAttackTarget().isMoving())
				{
					range -= 100;
				}
				if(range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
			}
			return;
		}

		melee(npc.getPrimarySkillId());
	}

	private void melee(int type)
	{
		if(type != 0)
		{
			switch(type)
			{
				case -1:
					if(_skillrender.getGeneralskills() != null)
					{
						for(L2Skill sk : _skillrender.getGeneralskills())
						{
							if(cast(sk))
							{
								return;
							}
						}
					}
					break;
				case 1:
					for(L2Skill sk : _skillrender.getAtkSkills())
					{
						if(cast(sk))
						{
							return;
						}
					}
					break;
				default:
					for(L2Skill sk : _skillrender.getGeneralskills())
					{
						if(sk.getId() == getActiveChar().getPrimarySkillId())
						{
							if(cast(sk))
							{
								return;
							}
						}
					}
					break;
			}
		}

		_accessor.doAttack(getAttackTarget());
	}

	private boolean cast(L2Skill sk)
	{
		if(sk == null)
		{
			return false;
		}

		L2Attackable caster = getActiveChar();

		if(caster.isCastingNow() && !sk.isSimultaneousCast())
		{
			return false;
		}

		if(!checkSkillCastConditions(sk))
		{
			return false;
		}
		if(getAttackTarget() == null)
		{
			if(caster.getMostHated() != null)
			{
				setAttackTarget(caster.getMostHated());
			}
		}
		L2Character attackTarget = getAttackTarget();
		if(attackTarget == null)
		{
			return false;
		}
		double dist = Math.sqrt(caster.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		double dist2 = dist - attackTarget.getTemplate().getCollisionRadius(attackTarget);
		double range = caster.getPhysicalAttackRange() + caster.getTemplate().getCollisionRadius(caster) + attackTarget.getTemplate().getCollisionRadius(attackTarget);
		double srange = sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster);
		if(attackTarget.isMoving())
		{
			dist2 -= 30;
		}

		switch(sk.getSkillType())
		{

			case BUFF:
				if(caster.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					// L2Object target = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					// _actor.setTarget(target);
					return true;
				}
				// ----------------------------------------
				//If actor already have buff, start looking at others same faction mob to cast
				if(sk.getTargetType() == L2TargetType.TARGET_SELF)
				{
					return false;
				}
				if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, true);
					if(target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				if(canParty(sk))
				{
					clientStopMoving(null);
					L2Object targets = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}
				break;
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case HEAL_STATIC:
			case BALANCE_LIFE:
				double percentage = caster.getCurrentHp() / caster.getMaxHp() * 100;
				if(caster.isMinion() && sk.getTargetType() != L2TargetType.TARGET_SELF)
				{
					L2Character leader = caster.getLeader();
					if(leader != null && !leader.isDead() && Rnd.getChance(100 - leader.getCurrentHp() / leader.getMaxHp() * 100))
					{
						if(!Util.checkIfInRange(sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster) + leader.getTemplate().getCollisionRadius(leader), caster, leader, false) && !isParty(sk) && !caster.isMovementDisabled())
						{
							moveToPawn(leader, sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster) + leader.getTemplate().getCollisionRadius(leader));
						}
						if(GeoEngine.getInstance().canSeeTarget(caster, leader))
						{
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}
				}
				if(Rnd.getChance((100 - percentage) / 3))
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}

				if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					for(L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster)))
					{
						if(!(obj instanceof L2Attackable) || obj.isDead())
						{
							continue;
						}

						L2Attackable targets = (L2Attackable) obj;
						if(caster.getFactionId() != null && !caster.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
						if(Rnd.getChance((100 - percentage) / 10))
						{
							if(GeoEngine.getInstance().canSeeTarget(caster, targets))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				if(isParty(sk))
				{
					for(L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius() + caster.getTemplate().getCollisionRadius(caster)))
					{
						if(!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = (L2Npc) obj;
						if(caster.getFactionId() != null && targets.getFactionId().equals(caster.getFactionId()))
						{
							if(obj.getCurrentHp() < obj.getMaxHp() && Rnd.getChance(20))
							{
								clientStopMoving(null);
								caster.setTarget(caster);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			case RESURRECT:
				if(!isParty(sk))
				{
					if(caster.isMinion() && sk.getTargetType() != L2TargetType.TARGET_SELF)
					{
						L2Character leader = caster.getLeader();
						if(leader != null && leader.isDead())
						{
							if(!Util.checkIfInRange(sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster) + leader.getTemplate().getCollisionRadius(leader), caster, leader, false) && !isParty(sk) && !caster.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster) + leader.getTemplate().getCollisionRadius(leader));
							}
						}
						if(GeoEngine.getInstance().canSeeTarget(caster, leader))
						{
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}

					for(L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().getCollisionRadius(caster)))
					{
						if(!(obj instanceof L2Attackable) || !obj.isDead())
						{
							continue;
						}

						L2Attackable targets = (L2Attackable) obj;
						if(caster.getFactionId() != null && !caster.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						if(Rnd.getChance(10))
						{
							if(GeoEngine.getInstance().canSeeTarget(caster, targets))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				else if(isParty(sk))
				{
					for(L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius() + caster.getTemplate().getCollisionRadius(caster)))
					{
						if(!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = (L2Npc) obj;
						if(caster.getFactionId() != null && caster.getFactionId().equals(targets.getFactionId()))
						{
							if(obj.getCurrentHp() < obj.getMaxHp() && Rnd.getChance(20))
							{
								clientStopMoving(null);
								caster.setTarget(caster);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			case DEBUFF:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
				if(GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() && dist2 <= srange)
				{
					if(attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(canAOE(sk))
				{
					if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA || sk.getTargetType() == L2TargetType.TARGET_AURA_CORPSE_MOB)
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if((sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if(target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			case SLEEP:
				if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					if(!attackTarget.isDead() && dist2 <= srange)
					{
						if(dist2 > range || attackTarget.isMoving())
						{
							if(attackTarget.getFirstEffect(sk) == null)
							{
								clientStopMoving(null);
								// _actor.setTarget(attackTarget);
								caster.doCast(sk);
								return true;
							}
						}
					}

					L2Character target = effectTargetReconsider(sk, false);
					if(target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(canAOE(sk))
				{
					if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if((sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			case ROOT:
			case STUN:
			case FLY_UP:
			case PARALYZE:
			case KNOCK_DOWN:
			case KNOCK_BACK:
				if(GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && dist2 <= srange)
				{
					if(attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(canAOE(sk))
				{
					if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					else if((sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if(target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			case MUTE:
			case FEAR:
				if(GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && dist2 <= srange)
				{
					if(attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(canAOE(sk))
				{
					if(sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if((sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if(target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			case CANCEL:
			case NEGATE:
				// decrease cancel probability
				if(Rnd.get(50) != 0)
				{
					return true;
				}

				if(sk.getTargetType() == L2TargetType.TARGET_ONE)
				{
					if(attackTarget.getFirstEffect(L2EffectType.BUFF) != null && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					L2Character target = effectTargetReconsider(sk, false);
					if(target != null)
					{
						clientStopMoving(null);
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
						return true;
					}
				}
				else if(canAOE(sk))
				{
					if((sk.getTargetType() == L2TargetType.TARGET_AURA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AURA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget))

					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					else if((sk.getTargetType() == L2TargetType.TARGET_AREA || sk.getTargetType() == L2TargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2TargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case CPDAM:
			case MANADAM:
			case CPDAMPERCENT:
			{
				if(canAura(sk))
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
				if(GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead())
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}

				L2Character target = skillTargetReconsider(sk);
				if(target != null)
				{
					clientStopMoving(null);
					L2Object targets = attackTarget;
					caster.setTarget(target);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}
				break;
			}
			default:
				if(canAura(sk))
				{
					clientStopMoving(null);
					// L2Object targets = attackTarget;
					// _actor.setTarget(_actor);
					caster.doCast(sk);
					// _actor.setTarget(targets);
					return true;
				}

				if(GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange)
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}

				L2Character target = skillTargetReconsider(sk);
				if(target != null)
				{
					clientStopMoving(null);
					L2Object targets = attackTarget;
					caster.setTarget(target);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}

				break;
		}

		return false;
	}

	/**
	 * This AI task will start when ACTOR cannot move and attack range larger
	 * than distance
	 */
	private void movementDisable()
	{
		L2Attackable npc = getActiveChar();
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		try
		{
			if(npc.getTarget() == null)
			{
				npc.setTarget(getAttackTarget());
			}

			// TODO: Выдает длиннющий NPE. Игрок логоффается?О_о
			if(getAttackTarget() == null)
			{
				return;
			}

			dist = Math.sqrt(npc.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
			dist2 = dist - npc.getTemplate().getCollisionRadius(npc);
			range = npc.getPhysicalAttackRange() + npc.getTemplate().getCollisionRadius(npc) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget());
			if(getAttackTarget().isMoving())
			{
				dist -= 30;
				if(npc.isMoving())
				{
					dist -= 50;
				}
			}

			// Check if activeChar has any skill
			if(!_skillrender.getGeneralskills().isEmpty())
			{
				// -------------------------------------------------------------
				// Try to stop the target or disable the target as priority
				int random = Rnd.get(100);
				if(!_skillrender.getImmobiliseSkills().isEmpty() && !getAttackTarget().isImmobilized() && random < 2)
				{
					for(L2Skill sk : _skillrender.getImmobiliseSkills())
					{
						if(!checkSkillCastConditions(sk) || sk.getCastRange() + npc.getTemplate().getCollisionRadius(npc) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget()) <= dist2 && !canAura(sk))
						{
							continue;
						}
						if(!GeoEngine.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if(getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				// Same as Above, but with Mute/FEAR etc....
				if(!_skillrender.getCostOverTimeSkills().isEmpty() && random < 5)
				{
					for(L2Skill sk : _skillrender.getCostOverTimeSkills())
					{
						if(!checkSkillCastConditions(sk) || sk.getCastRange() + npc.getTemplate().getCollisionRadius(npc) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget()) <= dist2 && !canAura(sk))
						{
							continue;
						}
						if(!GeoEngine.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if(getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				if(!_skillrender.getDebuffSkills().isEmpty() && random < 8)
				{
					for(L2Skill sk : _skillrender.getDebuffSkills())
					{
						if(!checkSkillCastConditions(sk) || sk.getCastRange() + npc.getTemplate().getCollisionRadius(npc) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget()) <= dist2 && !canAura(sk))
						{
							continue;
						}
						if(!GeoEngine.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if(getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				// Some side effect skill like CANCEL or NEGATE
				if(!_skillrender.getNegativeSkills().isEmpty() && random < 9)
				{
					for(L2Skill sk : _skillrender.getNegativeSkills())
					{
						if(!checkSkillCastConditions(sk) || sk.getCastRange() + npc.getTemplate().getCollisionRadius(npc) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget()) <= dist2 && !canAura(sk))
						{
							continue;
						}
						if(!GeoEngine.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if(getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				// Start ATK SKILL when nothing can be done
				if(!_skillrender.getAtkSkills().isEmpty() && (npc.isMovementDisabled() || npc.getAiType() == AIType.MAGE || npc.getAiType() == AIType.HEALER))
				{
					for(L2Skill sk : _skillrender.getAtkSkills())
					{
						if(!checkSkillCastConditions(sk) || sk.getCastRange() + npc.getTemplate().getCollisionRadius(npc) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget()) <= dist2 && !canAura(sk))
						{
							continue;
						}
						if(!GeoEngine.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						npc.doCast(sk);
						// _actor.setTarget(target);
						return;
					}
				}
			}
			// timepass = timepass + 1;
			if(npc.isMovementDisabled())
			{
				// timepass = 0;
				targetReconsider();

				return;
			}

			if(dist > range || !GeoEngine.getInstance().canSeeTarget(npc, getAttackTarget()))
			{
				if(getAttackTarget().isMoving())
				{
					range -= 100;
				}
				if(range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
				return;

			}

			melee(npc.getPrimarySkillId());
		}
		catch(NullPointerException e)
		{
			setIntention(AI_INTENTION_ACTIVE);
			_log.log(Level.ERROR, this + " - failed executing movementDisable(): " + e.getMessage(), e);
		}
	}

	private boolean checkSkillCastConditions(L2Skill skill)
	{
		if(skill.getMpConsume() >= getActiveChar().getCurrentMp())
		{
			return false;
		}
		if(getActiveChar().isSkillDisabled(skill))
		{
			return false;
		}
		if(!skill.isStatic())
		{
			if(skill.isMagic())
			{
				if(getActiveChar().isMuted())
				{
					return false;
				}
			}
			else
			{
				if(getActiveChar().isPhysicalMuted())
				{
					return false;
				}
			}
		}
		return true;
	}

	private L2Character effectTargetReconsider(L2Skill sk, boolean positive)
	{
		if(sk == null)
		{
			return null;
		}
		L2Attackable actor = getActiveChar();
		if(sk.getSkillType() != L2SkillType.NEGATE || sk.getSkillType() != L2SkillType.CANCEL)
		{
			if(!positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;

				for(L2Character obj : actor.getAttackByList())
				{
					if(obj == null || obj.isDead() || !GeoEngine.getInstance().canSeeTarget(actor, obj) || obj.equals(getAttackTarget()))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().getCollisionRadius(actor);
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + obj.getTemplate().getCollisionRadius(obj);
						if(obj.isMoving())
						{
							dist2 -= 70;
						}
					}
					catch(NullPointerException e)
					{
						continue;
					}
					if(dist2 <= range)
					{
						if(getAttackTarget().getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}

				// ----------------------------------------------------------------------
				// If there is nearby Target with aggro, start going on random
				// target that is attackable
				for(L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if(obj.isDead() || !GeoEngine.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + obj.getTemplate().getCollisionRadius(obj);
						if(obj.isMoving())
						{
							dist2 -= 70;
						}
					}
					catch(NullPointerException e)
					{
						continue;
					}
					if(obj instanceof L2Attackable)
					{
						if(actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
						{
							if(dist2 <= range)
							{
								if(getAttackTarget().getFirstEffect(sk) == null)
								{
									return obj;
								}
							}
						}
					}
					if(obj instanceof L2PcInstance || obj instanceof L2Summon)
					{
						if(dist2 <= range)
						{
							if(getAttackTarget().getFirstEffect(sk) == null)
							{
								return obj;
							}
						}
					}
				}
			}
			else if(positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				for(L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if(!(obj instanceof L2Attackable) || obj.isDead() || !GeoEngine.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}

					L2Attackable targets = (L2Attackable) obj;
					if(actor.getFactionId() != null && !actor.getFactionId().equals(targets.getFactionId()))
					{
						continue;
					}

					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().getCollisionRadius(actor);
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + obj.getTemplate().getCollisionRadius(obj);
						if(obj.isMoving())
						{
							dist2 -= 70;
						}
					}
					catch(NullPointerException e)
					{
						continue;
					}
					if(dist2 <= range)
					{
						if(obj.getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}
			}
		}
		else
		{
			double dist = 0;
			double dist2 = 0;
			int range = 0;
			range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget());
			for(L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
			{
				if(obj == null || obj.isDead() || !GeoEngine.getInstance().canSeeTarget(actor, obj))
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius(actor);
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + obj.getTemplate().getCollisionRadius(obj);
					if(obj.isMoving())
					{
						dist2 -= 70;
					}
				}
				catch(NullPointerException e)
				{
					continue;
				}
				if(obj instanceof L2Attackable)
				{
					if(actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if(dist2 <= range)
						{
							if(getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
							{
								return obj;
							}
						}
					}
				}
				if(obj instanceof L2PcInstance || obj instanceof L2Summon)
				{

					if(dist2 <= range)
					{
						if(getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
						{
							return obj;
						}
					}
				}
			}
		}
		return null;
	}

	private L2Character skillTargetReconsider(L2Skill sk)
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		if(actor == null)
		{
			return null;
		}

		for(L2Character obj : actor.getHateList())
		{
			if(obj == null || !GeoEngine.getInstance().canSeeTarget(actor, obj) || obj.isDead())
			{
				continue;
			}
			try
			{
				actor.setTarget(getAttackTarget());
				dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
				dist2 = dist - actor.getTemplate().getCollisionRadius(actor);
				range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget());
				//if(obj.isMoving())
				//	dist2 = dist2 - 40;
			}
			catch(NullPointerException e)
			{
				continue;
			}
			if(dist2 <= range)
			{
				return obj;
			}
		}
		if(!(actor instanceof L2GuardInstance))
		{
			for(L2Object target : actor.getKnownList().getKnownObjects().values())
			{
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY()));
					dist2 = dist;
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius(actor) + getAttackTarget().getTemplate().getCollisionRadius(getAttackTarget());
					//if(obj.isMoving())
					// dist2 = dist2 - 40;
				}
				catch(NullPointerException e)
				{
					continue;
				}
				L2Character obj = null;
				if(target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				if(obj == null || !GeoEngine.getInstance().canSeeTarget(actor, obj) || dist2 > range)
				{
					continue;
				}
				if(obj instanceof L2PcInstance)
				{
					return obj;
				}
				if(obj instanceof L2Attackable)
				{
					if(actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						return obj;
					}
					if(actor.getIsChaos() != 0)
					{
						if(((L2Attackable) obj).getFactionId() != null && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}

						return obj;
					}
				}
				if(obj instanceof L2Summon)
				{
					return obj;
				}
			}
		}
		return null;
	}

	private void targetReconsider()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();
		if(actor.getHateList() != null)
		{
			for(L2Character obj : actor.getHateList())
			{
				if(obj == null || !GeoEngine.getInstance().canSeeTarget(actor, obj) || obj.isDead() || !obj.equals(MostHate) || obj.equals(actor))
				{
					continue;
				}
				try
				{
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius(actor);
					range = actor.getPhysicalAttackRange() + actor.getTemplate().getCollisionRadius(actor) + obj.getTemplate().getCollisionRadius(obj);
					if(obj.isMoving())
					{
						dist2 -= 70;
					}
				}
				catch(NullPointerException e)
				{
					continue;
				}

				if(dist2 <= range)
				{
					if(MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					return;
				}
			}
		}
		if(!(actor instanceof L2GuardInstance))
		{
			for(L2Object target : actor.getKnownList().getKnownObjects().values())
			{
				L2Character obj = null;
				if(target instanceof L2Character)
				{
					obj = (L2Character) target;
				}

				if(obj == null || !GeoEngine.getInstance().canSeeTarget(actor, obj) || obj.isDead() || !obj.equals(MostHate) || obj.equals(actor) || obj.equals(getAttackTarget()))
				{
					continue;
				}
				if(obj instanceof L2PcInstance)
				{
					if(MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if(obj instanceof L2Attackable)
				{
					if(actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						actor.setTarget(obj);
					}
					if(actor.getIsChaos() != 0)
					{
						if(((L2Attackable) obj).getFactionId() != null && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}

						if(MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if(obj instanceof L2Summon)
				{
					if(MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}

	private void aggroReconsider()
	{
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();

		if(actor.getHateList() != null)
		{

			int rand = Rnd.get(actor.getHateList().size());
			int count = 0;
			for(L2Character obj : actor.getHateList())
			{
				if(count < rand)
				{
					count++;
					continue;
				}

				if(obj == null || !GeoEngine.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj.equals(getAttackTarget()) || obj.equals(actor))
				{
					continue;
				}

				try
				{
					actor.setTarget(getAttackTarget());
				}
				catch(NullPointerException e)
				{
					continue;
				}
				if(MostHate != null)
				{
					actor.addDamageHate(obj, 0, actor.getHating(MostHate));
				}
				else
				{
					actor.addDamageHate(obj, 0, 2000);
				}
				actor.setTarget(obj);
				setAttackTarget(obj);
				return;
			}
		}

		if(!(actor instanceof L2GuardInstance))
		{
			for(L2Object target : actor.getKnownList().getKnownObjects().values())
			{
				L2Character obj = null;
				if(target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				else
				{
					continue;
				}
				if(obj == null || !GeoEngine.getInstance().canSeeTarget(actor, obj) || obj.isDead() || !obj.equals(MostHate) || obj.equals(actor))
				{
					continue;
				}
				if(obj instanceof L2PcInstance)
				{
					if(MostHate != null && !MostHate.isDead())
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if(obj instanceof L2Attackable)
				{
					if(actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if(MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
					}
					if(actor.getIsChaos() != 0)
					{
						if(((L2Attackable) obj).getFactionId() != null && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}

						if(MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if(obj instanceof L2Summon)
				{
					if(MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}

	private List<L2Skill> longRangeSkillRender()
	{
		longRangeSkills = _skillrender.getLongRangeSkills();
		if(longRangeSkills.isEmpty())
		{
			longRangeSkills = getActiveChar().getLongRangeSkill();
		}
		return longRangeSkills;
	}

	private List<L2Skill> shortRangeSkillRender()
	{
		shortRangeSkills = _skillrender.getShortRangeSkills();
		if(shortRangeSkills.isEmpty())
		{
			shortRangeSkills = getActiveChar().getShortRangeSkill();
		}
		return shortRangeSkills;
	}

	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary),
	 * Calculate attack timeout, Start a new Attack and Launch Think Event.<BR>
	 * <BR>
	 *
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();

		// self and buffs
		if(lastBuffTick + 30 < GameTimeController.getInstance().getGameTicks())
		{
			for(L2Skill sk : _skillrender.getBuffSkills())
			{
				if(cast(sk))
				{
					break;
				}
			}
			lastBuffTick = GameTimeController.getInstance().getGameTicks();
		}

		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}

	/**
	 * Manage AI thinking actions of a L2Attackable.<BR><BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if(_thinking || getActiveChar().isAllSkillsDisabled())
		{
			return;
		}

		// Start thinking action
		_thinking = true;

		try
		{
			// Manage AI thinks of a L2Attackable
			switch(getIntention())
			{
				case AI_INTENTION_ACTIVE:
					thinkActive();
					break;
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, this + " -  onEvtThink() failed: " + e.getMessage(), e);
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event Attacked.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
	 *
	 * @param attacker The L2Character that attacks the actor
	 *
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		L2Attackable me = getActiveChar();

		// Check if mob should attack back
		if(me.isNoAttackingBack())
		{
			return;
		}

		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();

		// Set the _globalAggro to 0 to permit attack even just after spawn
		if(_globalAggro < 0)
		{
			_globalAggro = 0;
		}

		// Add the attacker to the _aggroList of the actor
		me.addDamageHate(attacker, 0, 1);

		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if(!me.isRunning())
		{
			me.setRunning();
		}

		// Set the Intention to AI_INTENTION_ATTACK
		if(getIntention() != AI_INTENTION_ATTACK || me.getMostHated() != getAttackTarget())
		{
			setIntention(AI_INTENTION_ATTACK, attacker);
		}

		if(me instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) me;

			if(master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}

			master = master.getLeader();
			if(master != null && master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
				master.getAI().onEvtAttacked(attacker);
			}
		}

		super.onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present </li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
	 *
	 * @param aggro The value of hate to add to the actor against the target
	 *
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = getActiveChar();

		if(target != null)
		{
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);

			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if(getIntention() != AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if(!me.isRunning())
				{
					me.setRunning();
				}

				setIntention(AI_INTENTION_ATTACK, target);
			}

			if(me instanceof L2MonsterInstance)
			{
				L2MonsterInstance master = (L2MonsterInstance) me;

				if(master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}

				master = master.getLeader();
				if(master != null && master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
			}
		}
	}

	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}

	/**
	 * @return Returns the timepass.
	 */
	public int getTimepass()
	{
		return timepass;
	}

	/**
	 * @param TP The timepass to set.
	 */
	public void setTimepass(int TP)
	{
		timepass = TP;
	}

	public L2Attackable getActiveChar()
	{
		return (L2Attackable) _actor;
	}
}
