/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.player.duel.DuelManager;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.l2skills.L2SkillDrain;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.Future;

public class L2CubicInstance
{
	// Type of Cubics
	public static final int CUBIC_DD = 1;
	public static final int CUBIC_DRAIN = 2;
	public static final int CUBIC_HEAL = 3;
	public static final int CUBIC_POISON = 4;
	public static final int CUBIC_DEBUF = 5;
	public static final int CUBIC_PARALYZE = 6;
	public static final int CUBIC_WATER_DOT = 7;
	public static final int CUBIC_SHOCK = 8;
	public static final int CUBIC_ATTRACT = 9;
	public static final int CUBIC_TEMPLEK_SMART = 10;
	public static final int CUBIC_SHILLIENK_SMART = 11;
	public static final int CUBIC_WARLOCK_SMART = 12;
	public static final int CUBIC_ELEMENTALS_SMART = 13;
	public static final int CUBIC_PHANTOMS_SMART = 14;
	public static final int CUBIC_AVENGE = 15;
	public static final int CUBIC_KNIGHT = 16;
	public static final int CUBIC_HEALER = 17;
	public static final int CUBIC_POEM = 18;
	public static final int CUBIC_MENTAL = 19;
	public static final int CUBIC_SPIRIT = 20;
	public static final int CUBIC_WHAMMY = 21;
	// Max range of cubic skills
	public static final int MAX_MAGIC_RANGE = 900;
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_KNIGHT = 10056;
	public static final int SKILL_CUBIC_HEALER = 11807;
	public static final int SKILL_CUBIC_CURE = 5579;
	public static final int SKILL_CUBIC_AVENGE_CDB = 11292;
	public static final int SKILL_POEM_CUBIC_HEAL = 10083;
	public static final int SKILL_POEM_CUBIC_GREAT_HEAL = 10082;
	public static final int SKILL_MENTAL_CUBIC_RECHARGE = 10084;
	public static final int SKILL_MENTAL_CUBIC_GREAT_RECHARGE = 10089;
	protected static final Logger _log = LogManager.getLogger(L2CubicInstance.class);
	protected L2PcInstance _owner;
	protected L2Character _target;

	protected int _id;
	protected int _cubicPower;
	protected int _activationtime;
	protected int _activationchance;
	protected int _maxcount;
	protected int _currentcount;
	protected boolean _active;
	protected List<L2Skill> _skills = new FastList<>();
	private boolean _givenByOther;
	private Future<?> _disappearTask;
	private Future<?> _actionTask;

	public L2CubicInstance(L2PcInstance owner, int id, int level, int cubicPower, int activationtime, int activationchance, int maxcount, int totallifetime, boolean givenByOther)
	{
		_owner = owner;
		_id = id;
		_cubicPower = cubicPower;
		_activationtime = activationtime * 1000;
		_activationchance = activationchance;
		_maxcount = maxcount;
		_currentcount = 0;
		_active = false;
		_givenByOther = givenByOther;

		switch(_id)
		{
			case CUBIC_DD:
				_skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			case CUBIC_DRAIN:
				_skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			case CUBIC_HEAL:
				_skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			case CUBIC_POISON:
				_skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			case CUBIC_DEBUF:
				_skills.add(SkillTable.getInstance().getInfo(4053, level));
				_skills.add(SkillTable.getInstance().getInfo(4054, level));
				_skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			case CUBIC_PARALYZE:
				_skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			case CUBIC_WATER_DOT:
				_skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			case CUBIC_SHOCK:
				_skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			case CUBIC_ATTRACT:
				_skills.add(SkillTable.getInstance().getInfo(5115, level));
				_skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			case CUBIC_TEMPLEK_SMART:
				_skills.add(SkillTable.getInstance().getInfo(4053, 8));
				_skills.add(SkillTable.getInstance().getInfo(4165, 9));
				break;
			case CUBIC_SHILLIENK_SMART:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8));
				_skills.add(SkillTable.getInstance().getInfo(5115, 4));
				break;
			case CUBIC_WARLOCK_SMART:
				_skills.add(SkillTable.getInstance().getInfo(4051, 7));
				_skills.add(SkillTable.getInstance().getInfo(4165, 9));
				break;
			case CUBIC_ELEMENTALS_SMART:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8));
				_skills.add(SkillTable.getInstance().getInfo(4166, 9));
				break;
			case CUBIC_PHANTOMS_SMART:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8));
				_skills.add(SkillTable.getInstance().getInfo(4052, 6));
				break;
			case CUBIC_AVENGE:
				_skills.add(SkillTable.getInstance().getInfo(SKILL_CUBIC_AVENGE_CDB, level));
				_skills.add(SkillTable.getInstance().getInfo(11293, level));
				_skills.add(SkillTable.getInstance().getInfo(11294, level));
				break;
			case CUBIC_KNIGHT:
				_skills.add(SkillTable.getInstance().getInfo(SKILL_CUBIC_KNIGHT, level));
				doAction();
				break;
			case CUBIC_HEALER:
				_skills.add(SkillTable.getInstance().getInfo(11807, level)); //heal cubic
				doAction();
				break;
			case CUBIC_POEM:
				_skills.add(SkillTable.getInstance().getInfo(10083, level));
				_skills.add(SkillTable.getInstance().getInfo(10082, level));
				doAction();
				break;
			case CUBIC_MENTAL:
				_skills.add(SkillTable.getInstance().getInfo(10084, level));
				_skills.add(SkillTable.getInstance().getInfo(10089, level));
				doAction();
				break;
			case CUBIC_SPIRIT:
				_skills.add(SkillTable.getInstance().getInfo(10085, level));
				break;
			case CUBIC_WHAMMY:
				_skills.add(SkillTable.getInstance().getInfo(10086, level));
				break;
		}
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new DisappearTask(), totallifetime); // disappear
	}

	public void doAction()
	{
		synchronized(this)
		{
			if(_active)
			{
				return;
			}
			_active = true;

			switch(_id)
			{
				case CUBIC_WATER_DOT:
				case CUBIC_PARALYZE:
				case CUBIC_SHOCK:
				case CUBIC_DD:
				case CUBIC_DEBUF:
				case CUBIC_DRAIN:
				case CUBIC_POISON:
				case CUBIC_ATTRACT:
				case CUBIC_WARLOCK_SMART:
				case CUBIC_ELEMENTALS_SMART:
				case CUBIC_PHANTOMS_SMART:
				case CUBIC_TEMPLEK_SMART:
				case CUBIC_SHILLIENK_SMART:
				case CUBIC_AVENGE:
				case CUBIC_SPIRIT:
				case CUBIC_WHAMMY:
					_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(_activationchance), 0, _activationtime);
					break;
				case CUBIC_HEAL:
				case CUBIC_HEALER:
				case CUBIC_POEM:
				case CUBIC_MENTAL:
					_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new HealTask(), 0, _activationtime);
					break;
				case CUBIC_KNIGHT:
					_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new BuffTask(_activationchance), 1000, _activationtime);
					break;
			}
		}
	}

	public int getId()
	{
		return _id;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public int getMCriticalHit()
	{
		// Magical Critical Rate for cubics is the base Magical Critical Rate of its owner
		return (int) (BaseStats.WIT.calcBonus(_owner) * 10);
	}

	public int getCubicPower()
	{
		return _cubicPower;
	}

	public void stopAction()
	{
		_target = null;
		if(_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
		_active = false;
	}

	public void cancelDisappear()
	{
		if(_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}

	/**
	 * this sets the enemy target for a cubic
	 */
	public void getCubicTarget()
	{
		try
		{
			_target = null;
			L2Object ownerTarget = _owner.getTarget();
			if(ownerTarget == null)
			{
				return;
			}

			EventManager.eventTarget(_owner, _target);

			// Duel targeting
			if(_owner.isInDuel())
			{
				L2PcInstance PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
				L2PcInstance PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();

				if(DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				{
					L2Party partyA = PlayerA.getParty();
					L2Party partyB = PlayerB.getParty();
					L2Party partyEnemy = null;

					if(partyA != null)
					{
						if(partyA.getMembers().contains(_owner))
						{
							if(partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else
					{
						if(PlayerA.equals(_owner))
						{
							if(partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							_target = PlayerA;
						}
					}
					if(_target.equals(PlayerA) || _target.equals(PlayerB))
					{
						if(_target.equals(ownerTarget))
						{
							return;
						}
					}
					if(partyEnemy != null)
					{
						if(partyEnemy.getMembers().contains(ownerTarget))
						{
							_target = (L2Character) ownerTarget;
						}
						return;
					}
				}
				if(!PlayerA.equals(_owner) && ownerTarget.equals(PlayerA))
				{
					_target = PlayerA;
					return;
				}
				if(!PlayerB.equals(_owner) && ownerTarget.equals(PlayerB))
				{
					_target = PlayerB;
					return;
				}
				_target = null;
				return;
			}
			// Olympiad targeting
			if(_owner.getOlympiadController().isParticipating() && _owner.getOlympiadController().isPlayingNow())
			{
				if(ownerTarget instanceof L2Playable)
				{
					if(_owner.getOlympiadController().isOpponent(ownerTarget.getActingPlayer()))
					{
						_target = (L2Character) ownerTarget;
					}
				}
				return;
			}
			// test owners target if it is valid then use it
			if(ownerTarget instanceof L2Character && !(ownerTarget instanceof L2Summon && ((L2Summon) ownerTarget).getOwner().equals(_owner)) && !ownerTarget.equals(_owner))
			{
				// target mob which has aggro on you or your summon
				if(ownerTarget instanceof L2Attackable)
				{
					if(((L2Attackable) ownerTarget).getAggroList().get(_owner) != null && !((L2Attackable) ownerTarget).isDead())
					{
						_target = (L2Character) ownerTarget;
						return;
					}
					if(!_owner.getPets().isEmpty())
					{
						for(L2Summon pet : _owner.getPets())
						{
							if(((L2Attackable) ownerTarget).getAggroList().get(pet) != null && !((L2Attackable) ownerTarget).isDead())
							{
								_target = (L2Character) ownerTarget;
								return;
							}
						}
					}
				}

				// get target in pvp or in siege
				L2PcInstance enemy = null;

				if(_owner.getPvPFlagController().isFlagged() && !_owner.isInsideZone(L2Character.ZONE_PEACE) || _owner.isInsideZone(L2Character.ZONE_PVP))
				{
					if(!((L2Character) ownerTarget).isDead())
					{
						enemy = ownerTarget.getActingPlayer();
					}

					if(enemy != null)
					{
						boolean targetIt = true;

						if(_owner.getParty() != null)
						{
							if(_owner.getParty().getMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if(_owner.getParty().getCommandChannel() != null)
							{
								if(_owner.getParty().getCommandChannel().getMembers().contains(enemy))
								{
									targetIt = false;
								}
							}
						}
						if(_owner.getClan() != null && !_owner.isInsideZone(L2Character.ZONE_PVP))
						{
							if(_owner.getClan().isMember(enemy.getObjectId()))
							{
								targetIt = false;
							}
							if(_owner.getAllyId() > 0 && enemy.getAllyId() > 0)
							{
								if(_owner.getAllyId() == enemy.getAllyId())
								{
									targetIt = false;
								}
							}
						}
						if(!enemy.getPvPFlagController().isFlagged() && !enemy.isInsideZone(L2Character.ZONE_PVP))
						{
							targetIt = false;
						}
						if(enemy.isInsideZone(L2Character.ZONE_PEACE))
						{
							targetIt = false;
						}
						if(_owner.getSiegeSide() != PlayerSiegeSide.NONE && _owner.getSiegeSide() == enemy.getSiegeSide())
						{
							targetIt = false;
						}
						if(!enemy.isVisible())
						{
							targetIt = false;
						}

						if(targetIt)
						{
							_target = enemy;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	public void useCubicContinuous(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for(L2Character target : (L2Character[]) targets)
		{
			if(target == null || target.isDead())
			{
				continue;
			}

			if(skill.isOffensive())
			{
				byte shld = Shield.calcShldUse(activeCubic._owner, target, skill);
				boolean acted = Skills.calcCubicSkillSuccess(activeCubic, target, skill, shld);
				if(!acted)
				{
					activeCubic._owner.sendPacket(SystemMessageId.ATTACK_FAILED);
					continue;
				}
			}

			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if(target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && skill.getSkillType() == L2SkillType.DEBUFF && activeCubic._owner.getDuelId() == ((L2PcInstance) target).getDuelId())
			{
				DuelManager dm = DuelManager.getInstance();
				for(L2Effect debuff : skill.getEffects(activeCubic._owner, target))
				{
					if(debuff != null)
					{
						dm.onBuff((L2PcInstance) target, debuff);
					}
				}
			}
			else
			{
				skill.getEffects(activeCubic, target, null);
			}
		}
	}

	public void useCubicMdam(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for(L2Character target : (L2Character[]) targets)
		{
			if(target == null)
			{
				continue;
			}

			if(target.isAlikeDead())
			{
				if(target instanceof L2PcInstance)
				{
					target.stopFakeDeath(true);
				}
				else
				{
					continue;
				}
			}

			boolean mcrit = MagicalDamage.calcMCrit(activeCubic.getMCriticalHit());
			byte shld = Shield.calcShldUse(activeCubic._owner, target, skill);
			int damage = (int) MagicalDamage.calcMagicDam(activeCubic, target, skill, mcrit, shld);

			/*
		    * If target is reflecting the skill then no damage is done
            * Ignoring vengance-like reflections
            */
			if((Reflect.calcSkillReflect(target, skill) & Variables.SKILL_REFLECT_SUCCEED) > 0)
			{
				damage = 0;
			}

			if(damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate,
				// sending message...)
				CancelAttack.calcAtkBreak(target, damage);

				activeCubic._owner.sendDamageMessage(target, damage, mcrit, false, false);

				if(skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					if(target.getFirstEffect(skill) != null)
					{
						target.removeEffect(target.getFirstEffect(skill));
					}
					if(Skills.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						skill.getEffects(activeCubic, target, null);
					}
				}

				target.reduceCurrentHp(damage, activeCubic._owner, skill);
			}
		}
	}

	public void useCubicDisabler(L2SkillType type, L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for(L2Character target : (L2Character[]) targets)
		{
			if(target == null || target.isDead()) // bypass if target is null or dead
			{
				continue;
			}

			byte shld = Shield.calcShldUse(activeCubic._owner, target, skill);

			switch(type)
			{
				case CANCEL_DEBUFF:
					L2Effect[] effects = target.getAllEffects();

					if(effects == null || effects.length == 0)
					{
						break;
					}

					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
					for(L2Effect e : effects)
					{
						if(e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects())
						{
							// Do not remove raid curse skills
							if(e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082)
							{
								e.exit();
								if(count > -1)
								{
									count++;
								}
							}
						}
					}
					break;
				case STUN:
				case PARALYZE:
				case ROOT:
					if(Skills.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						// if this is a debuff let the duel manager know about
						// it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if(target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && skill.getSkillType() == L2SkillType.DEBUFF && activeCubic._owner.getDuelId() == ((L2PcInstance) target).getDuelId())
						{
							DuelManager dm = DuelManager.getInstance();
							for(L2Effect debuff : skill.getEffects(activeCubic._owner, target))
							{
								if(debuff != null)
								{
									dm.onBuff((L2PcInstance) target, debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic, target, null);
						}
					}
					break;
				case AGGDAMAGE:
					if(Skills.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						if(target instanceof L2Attackable)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeCubic._owner, (int) (150 * skill.getPower() / (target.getLevel() + 7)));
						}
						skill.getEffects(activeCubic, target, null);
					}
					break;
			}
		}
	}

	/**
	 * @return {@code true} if the target is inside of the owner's max Cubic range
	 */
	public boolean isInCubicRange(L2Character owner, L2Character target)
	{
		if(owner == null || target == null)
		{
			return false;
		}

		int x;
		int y;
		int z;
		// temporary range check until real behavior of cubics is known/coded
		int range = MAX_MAGIC_RANGE;

		x = owner.getX() - target.getX();
		y = owner.getY() - target.getY();
		z = owner.getZ() - target.getZ();

		return x * x + y * y + z * z <= range * range;
	}

	/**
	 * this sets the friendly target for a cubic
	 */
	public void cubicTargetForHeal()
	{
		L2Character target = null;
		double percentleft = 100.0;
		L2Party party = _owner.getParty();

		// if owner is in a duel but not in a party duel, then it is the same as
		// he does not have a
		// party
		if(_owner.isInDuel())
		{
			if(!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				party = null;
			}
		}

		if(party != null && !_owner.getOlympiadController().isParticipating())
		{
			// Get all visible objects in a spheric area near the L2Character
			// Get a list of Party Members
			List<L2PcInstance> partyList = party.getMembers();
			for(L2Character partyMember : partyList)
			{
				if(!partyMember.isDead())
				{
					// if party member not dead, check if he is in castrange of
					// heal cubic
					if(isInCubicRange(_owner, partyMember))
					{
						// member is in cubic casting range, check if he need
						// heal and if he have
						// the lowest HP
						if(partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if(percentleft > partyMember.getCurrentHp() / partyMember.getMaxHp())
							{
								percentleft = partyMember.getCurrentHp() / partyMember.getMaxHp();
								target = partyMember;
							}
						}
					}
				}
				if(!partyMember.getPets().isEmpty())
				{
					for(L2Summon pet : partyMember.getPets())
					{
						if(pet.isDead())
						{
							continue;
						}
						// if party member's pet not dead, check if it is in
						// castrange of heal cubic
						if(!isInCubicRange(_owner, pet))
						{
							continue;
						}
						// member's pet is in cubic casting range, check if he need
						// heal and if he have
						// the lowest HP
						if(pet.getCurrentHp() < pet.getMaxHp())
						{
							if(percentleft > pet.getCurrentHp() / pet.getMaxHp())
							{
								percentleft = pet.getCurrentHp() / pet.getMaxHp();
								target = pet;
							}
						}
					}
				}
			}
		}
		_target = target;
	}

	public boolean givenByOther()
	{
		return _givenByOther;
	}

	private class Action implements Runnable
	{
		private int _chance;

		Action(int chance)
		{
			_chance = chance;
		}

		@Override
		public void run()
		{
			try
			{
				if(_owner.isDead() || !_owner.isOnline())
				{
					stopAction();
					_owner.removeCubic(_id);
					_owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}
				if(!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner))
				{
					if(_owner.getPets().isEmpty())
					{
						boolean _stopAction = false;
						for(L2Summon pet : _owner.getPets())
						{
							if(!AttackStanceTaskManager.getInstance().hasAttackStanceTask(pet))
							{
								_stopAction = true;
							}
						}
						if(_stopAction)
						{
							stopAction();
							return;
						}
					}
					else
					{
						stopAction();
						return;
					}
				}
				// The cubic has already reached its limit and it will stay idle until its lifetime ends.
				if(_maxcount > -1 && _currentcount >= _maxcount)
				{
					stopAction();
					return;
				}
				// Smart Cubic debuff cancel is 100%
				boolean isCleaningCube = false;
				L2Skill skill = null;

				if(_id >= CUBIC_TEMPLEK_SMART && _id <= CUBIC_PHANTOMS_SMART)
				{
					L2Effect[] effects = _owner.getAllEffects();

					for(L2Effect e : effects)
					{
						if(e != null && e.getSkill().isDebuff() && e.getSkill().canBeDispeled())
						{
							isCleaningCube = true;
							e.exit();
						}
					}
				}

				if(isCleaningCube)
				{
					// Smart Cubic debuff cancel is needed, no other skill is
					// used in this
					// activation period
					MagicSkillUse msu = new MagicSkillUse(_owner, _owner, SKILL_CUBIC_CURE, 1, 0, 0);
					_owner.broadcastPacket(msu);

					// The cubic has done an action, increase the currentcount
					_currentcount++;
				}
				else if(Rnd.getChance(_chance))
				{
					skill = _skills.get(Rnd.get(_skills.size()));
					if(skill != null)
					{
						if(skill.getId() == SKILL_CUBIC_HEAL || skill.getId() == SKILL_CUBIC_AVENGE_CDB /* Не знаю может ли он снимать дебафы у игроков в пати */)
						{
							// friendly skill, so we look a target in owner's party
							cubicTargetForHeal();
						}
						else
						{
							// offensive skill, we look for an enemy target
							getCubicTarget();
							if(!isInCubicRange(_owner, _target))
							{
								_target = null;
							}
						}
						L2Character target = _target; // copy to avoid npe
						if(target != null && !target.isDead())
						{
							_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0));

							L2SkillType type = skill.getSkillType();
							ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
							L2Character[] targets = {target};

							if(type == L2SkillType.PARALYZE || type == L2SkillType.STUN || type == L2SkillType.ROOT || type == L2SkillType.AGGDAMAGE)
							{
								useCubicDisabler(type, L2CubicInstance.this, skill, targets);
							}
							else if(type == L2SkillType.MDAM)
							{
								useCubicMdam(L2CubicInstance.this, skill, targets);
							}
							else if(type == L2SkillType.POISON || type == L2SkillType.DEBUFF || type == L2SkillType.DOT)
							{
								useCubicContinuous(L2CubicInstance.this, skill, targets);
							}
							else if(type == L2SkillType.DRAIN)
							{
								((L2SkillDrain) skill).useCubicSkill(L2CubicInstance.this, targets);
							}
							else
							{
								handler.useSkill(_owner, skill, targets);
							}

							// The cubic has done an action, increase the currentcount
							_currentcount++;
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	private class HealTask implements Runnable
	{

		@Override
		public void run()
		{
			if(_owner.isDead() || !_owner.isOnline())
			{
				stopAction();
				_owner.removeCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			try
			{
				L2Skill skill = null;

				// Base chance 10% of usage great skill
				double chance = Rnd.get();
				for(L2Skill sk : _skills)
				{
					/* Для хиляющих скиллов кубика. */
					switch(sk.getId())
					{
						case SKILL_CUBIC_HEAL:
						case SKILL_CUBIC_HEALER:
							skill = sk;
							break;
						case SKILL_POEM_CUBIC_HEAL:
						case SKILL_MENTAL_CUBIC_RECHARGE:
							if(chance > 0.1)
							{
								skill = sk;
							}
							break;
						case SKILL_POEM_CUBIC_GREAT_HEAL:
						case SKILL_MENTAL_CUBIC_GREAT_RECHARGE:
							if(chance <= 0.1)
							{
								skill = sk;
							}
							break;
					}

					if(skill != null)
					{
						break;
					}
				}

				switch(skill.getId())
				{
					case SKILL_CUBIC_HEAL:
						cubicTargetForHeal();

						L2Character target = _target;

						if(target != null && !target.isDead())
						{
							if(target.getMaxHp() - target.getCurrentHp() > skill.getPower())
							{
								L2Character[] targets = {target};
								ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
								if(handler != null)
								{
									handler.useSkill(_owner, skill, targets);
								}
								else
								{
									skill.useSkill(_owner, targets);
								}

								MagicSkillUse msu = new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0);
								_owner.broadcastPacket(msu);
							}
						}
						break;
					case SKILL_MENTAL_CUBIC_RECHARGE:
					case SKILL_MENTAL_CUBIC_GREAT_RECHARGE:
						if(_owner != null && !_owner.isDead())
						{
							if(_owner.getMaxMp() - _owner.getCurrentMp() > skill.getPower())
							{
								L2Character[] targets = {_owner};

								ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
								if(handler != null)
								{
									handler.useSkill(_owner, skill, targets);
								}
								else
								{
									skill.useSkill(_owner, targets);
								}
								_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, skill.getDisplayId(), skill.getLevel(), 0, 0));
							}
						}
						break;
					case SKILL_CUBIC_HEALER:
					case SKILL_POEM_CUBIC_HEAL:
					case SKILL_POEM_CUBIC_GREAT_HEAL:
						if(_owner != null && !_owner.isDead())
						{
							if(_owner.getMaxHp() - _owner.getCurrentHp() > skill.getPower())
							{
								L2Character[] targets = {_owner};

								ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
								if(handler != null)
								{
									handler.useSkill(_owner, skill, targets);
								}
								else
								{
									skill.useSkill(_owner, targets);
								}
								_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, skill.getDisplayId(), skill.getLevel(), 0, 0));
							}
						}
						break;
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	private class BuffTask implements Runnable
	{
		int _chance;

		private BuffTask(int chance)
		{
			_chance = chance;
		}

		@Override
		public void run()
		{
			if(_owner == null || _owner.isDead() || !_owner.isOnline())
			{
				stopAction();
				_owner.removeCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}

			// The cubic has already reached its limit and it will stay idle until its lifetime ends.
			if(_maxcount > -1 && _currentcount >= _maxcount)
			{
				stopAction();
				return;
			}
			try
			{
				if(Rnd.getChance(_chance) || _currentcount == 0)
				{
					L2Skill skill = null;

					for(L2Skill sk : _skills)
					{
						switch(sk.getId())
						{
							case SKILL_CUBIC_KNIGHT:
								skill = sk;
								break;
						}
					}

					if(skill != null)
					{
						switch(skill.getId())
						{
							case SKILL_CUBIC_KNIGHT:
								L2Character[] targets = {_owner};
								ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
								if(handler != null)
								{
									handler.useSkill(_owner, skill, targets);
								}
								else
								{
									skill.useSkill(_owner, targets);
								}

								_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, skill.getId(), skill.getLevel(), 0, 0));
								break;
						}
					}

					_currentcount++;
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	private class DisappearTask implements Runnable
	{

		@Override
		public void run()
		{
			stopAction();
			_owner.removeCubic(_id);
			_owner.broadcastUserInfo();
		}
	}
}
