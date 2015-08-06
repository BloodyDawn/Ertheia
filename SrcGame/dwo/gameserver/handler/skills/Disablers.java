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
package dwo.gameserver.handler.skills;

import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2AttackableAI;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.BlowDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This Handles Disabler skills
 *
 * @author _drunk_
 */
public class Disablers implements ISkillHandler
{
	protected static final Logger _log = LogManager.getLogger(L2Skill.class);
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.STUN, L2SkillType.ROOT, L2SkillType.SLEEP, L2SkillType.CONFUSION, L2SkillType.AGGDAMAGE,
		L2SkillType.AGGREDUCE, L2SkillType.AGGREDUCE_CHAR, L2SkillType.AGGREMOVE, L2SkillType.MUTE,
		L2SkillType.FAKE_DEATH, L2SkillType.CONFUSE_MOB_ONLY, L2SkillType.NEGATE, L2SkillType.CANCEL_DEBUFF,
		L2SkillType.PARALYZE, L2SkillType.ERASE, L2SkillType.BETRAY, L2SkillType.DISARM, L2SkillType.FLY_UP,
		L2SkillType.KNOCK_DOWN, L2SkillType.KNOCK_BACK
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2SkillType type = skill.getSkillType();

		byte shld = 0;
		boolean ss = activeChar.isSoulshotCharged(skill);
		boolean sps = activeChar.isSpiritshotCharged(skill);
		boolean bss = activeChar.isBlessedSpiritshotCharged(skill);

		for(L2Object obj : targets)
		{
			if(!(obj instanceof L2Character))
			{
				continue;
			}
			L2Character target = (L2Character) obj;
			if(target.isDead() || target.isInvul() && type != L2SkillType.NEGATE && !target.isParalyzed()) // bypass if target is null, dead or invul (excluding invul from Petrification)
			{
				continue;
			}

			shld = Shield.calcShldUse(activeChar, target, skill);

			switch(type)
			{
				case BETRAY:
					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
					break;
				case FAKE_DEATH:
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					break;
				case ROOT:
				case DISARM:
				case STUN:
					if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						if(activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					break;
				case SLEEP:
				case FLY_UP:
				case KNOCK_DOWN:
				case KNOCK_BACK:
				case PARALYZE: //use same as root for now
					if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						if(activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					break;
				case CONFUSION:
				case MUTE:
					if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						// stop same type effect if available
						for(L2Effect e : target.getAllEffects())
						{
							if(e != null && e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
						}
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						if(activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					break;
				case CONFUSE_MOB_ONLY:
					// do nothing if not on mob
					if(target instanceof L2Attackable)
					{
						if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
						{
							for(L2Effect e : target.getAllEffects())
							{
								if(e != null && e.getSkill().getSkillType() == type)
								{
									e.exit();
								}
							}
							skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
						}
						else
						{
							if(activeChar instanceof L2PcInstance)
							{
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
							}
						}
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					}
					break;
				case AGGDAMAGE:
					if(target instanceof L2Attackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) (150 * skill.getPower() / (target.getLevel() + 7)));
					}
					// TODO [Nemesiss] should this have 100% chance?
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					break;
				case AGGREDUCE:
					// these skills needs to be rechecked
					if(target instanceof L2Attackable)
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));

						double aggdiff = ((L2Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable) target).getHating(activeChar), target, skill);

						if(skill.getPower() > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) skill.getPower());
						}
						else if(aggdiff > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) aggdiff);
						}
					}
					// when fail, target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					break;
				case AGGREDUCE_CHAR:
					// these skills needs to be rechecked
					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						if(target instanceof L2Attackable)
						{
							L2Attackable targ = (L2Attackable) target;
							targ.stopHating(activeChar);
							if(targ.getMostHated() == null && targ.hasAI() && targ.getAI() instanceof L2AttackableAI)
							{
								((L2AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.clearAggroList();
								targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						if(activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					}
					break;
				case AGGREMOVE:
					// these skills needs to be rechecked
					if(target instanceof L2Attackable && !target.isRaid())
					{
						if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
						{
							if(skill.getTargetType() == L2TargetType.TARGET_UNDEAD)
							{
								if(target.isUndead())
								{
									((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
								}
							}
							else
							{
								((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
							}
						}
						else
						{
							if(activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
								sm.addCharName(target);
								sm.addSkillName(skill);
								activeChar.sendPacket(sm);
							}
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
						}
					}
					else
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					}
					break;
				case ERASE:
					// Doesn't affect siege golem or wild hog cannon
					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss) && !(target instanceof L2SiegeSummonInstance))
					{
						L2PcInstance summonOwner = ((L2Summon) target).getOwner();
						if(target instanceof L2Summon)
						{
							if(((L2Summon) target).isPhoenixBlessed())
							{
								if(((L2Summon) target).isNoblesseBlessed())
								{
									((L2Summon) target).stopNoblesseBlessing(null);
								}
							}
							else if(((L2Summon) target).isNoblesseBlessed())
							{
								((L2Summon) target).stopNoblesseBlessing(null);
							}
							else
							{
								target.stopAllEffectsExceptThoseThatLastThroughDeath();
							}
							target.getLocationController().decay();
							summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						}
					}
					else
					{
						if(activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					break;
				case CANCEL_DEBUFF:
				{
					L2Effect[] effects = target.getAllEffects();

					if(effects == null || effects.length == 0)
					{
						break;
					}

					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
					for(L2Effect e : effects)
					{
						if(e == null || !e.getSkill().isDebuff() || !e.getSkill().canBeDispeled())
						{
							continue;
						}

						e.exit();

						if(count > -1)
						{
							count++;
							if(count >= skill.getMaxNegatedEffects())
							{
								break;
							}
						}
					}
					if(skill.hasEffects())
					{
						skill.getEffects(activeChar, target);
					}
					break;
				}
				case CANCEL_STATS: // same than CANCEL but
					if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						L2Effect[] effects = target.getAllEffects();

						int max = skill.getMaxNegatedEffects();
						if(max == 0)
						{
							max = Integer.MAX_VALUE; //this is for RBcancells and stuff...
						}

						if(effects.length >= max)
						{
							effects = SortEffects(effects);
						}

						//for(int i = 0; i < effects.length;i++)
						//    activeChar.sendMessage(Integer.toString(effects[i].getSkill().getMagicLevel()));

						int count = 1;

						for(L2Effect e : effects)
						{
							// do not delete signet effects!
							switch(e.getEffectType())
							{
								case SIGNET_GROUND:
								case SIGNET_EFFECT:
									continue;
							}

							switch(e.getSkill().getId())
							{
								case 4082:
								case 4215:
								case 4515:
								case 5182:
								case 110:
								case 111:
								case 1323:
								case 1325:
									continue;
							}

							switch(e.getSkill().getSkillType())
							{
								case BUFF:
								case HEAL_PERCENT:
								case COMBATPOINTHEAL:
									break;
								default:
									continue;
							}

							double rate = 1 - count / max;
							if(rate < 0.33)
							{
								rate = 0.33;
							}
							else if(rate > 0.95)
							{
								rate = 0.95;
							}
							if(Rnd.get(1000) < rate * 1000)
							{

								boolean exit = false;
								for(L2SkillType skillType : skill.getNegateStats())
								{
									if(skillType == e.getSkillType())
									{
										exit = true;
										break;
									}
								}

								if(exit)
								{
									e.exit();
									if(count == max)
									{
										break;
									}

									count++;
								}
							}
						}
					}
					else
					{
						if(activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}

					break;
				case NEGATE:
					if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if(skill.getNegateId().length != 0)
					{
						for(int i = 0; i < skill.getNegateId().length; i++)
						{
							if(skill.getNegateId()[i] != 0)
							{
								target.stopSkillEffects(skill.getNegateId()[i]);
							}
						}
					}
					else if(skill.getNegateAbnormals() != null)
					{
						for(L2Effect effect : target.getAllEffects())
						{
							if(effect == null)
							{
								continue;
							}

							skill.getNegateAbnormals().keySet().stream().filter(negateAbnormalType -> negateAbnormalType.equalsIgnoreCase(effect.getAbnormalType()) && skill.getNegateAbnormals().get(negateAbnormalType) >= effect.getAbnormalLvl()).forEach(negateAbnormalType -> effect.exit());
						}
					}

					else // all others negate type skills
					{
						int removedBuffs = skill.getMaxNegatedEffects() > 0 ? 0 : -2;

						for(L2SkillType skillType : skill.getNegateStats())
						{
							if(removedBuffs > skill.getMaxNegatedEffects())
							{
								break;
							}

							switch(skillType)
							{
								case BUFF:
									int lvlmodifier = 52 + (skill.getMagicLevel() << 1);
									if(skill.getMagicLevel() == 12)
									{
										lvlmodifier = ExperienceTable.getInstance().getMaxLevel() - 1;
									}
									int landrate = 90;
									if(target.getLevel() - lvlmodifier > 0)
									{
										landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
									}

									landrate = (int) activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);

									if(Rnd.getChance(landrate))
									{
										removedBuffs += negateEffect(target, L2SkillType.BUFF, -1, skill.getMaxNegatedEffects());
									}
									break;
								case HEAL:
									ISkillHandler Healhandler = SkillHandler.getInstance().getHandler(L2SkillType.HEAL);
									if(Healhandler == null)
									{
										_log.log(Level.ERROR, "Couldn't find skill handler for HEAL.");
										continue;
									}
									L2Character[] tgts = {target};
									Healhandler.useSkill(activeChar, skill, tgts);
									break;
								default:
									removedBuffs += negateEffect(target, skillType, skill.getNegateLvl(), skill.getMaxNegatedEffects());
									break;
							}//end switch
						}//end for
					}//end else

					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
			}//end switch

			//Possibility of a lethal strike
			BlowDamage.calcLethalHit(activeChar, target, skill);
		}//end for

		// self Effect :]
		if(skill.hasSelfEffects())
		{
			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if(effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}

		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	/**
	 * @param target
	 * @param type
	 * @param negateLvl
	 * @param maxRemoved
	 * @return
	 */
	private int negateEffect(L2Character target, L2SkillType type, int negateLvl, int maxRemoved)
	{
		return negateEffect(target, type, negateLvl, 0, maxRemoved);
	}

	/**
	 * @param target
	 * @param type
	 * @param negateLvl
	 * @param skillId
	 * @param maxRemoved
	 * @return
	 */
	private int negateEffect(L2Character target, L2SkillType type, int negateLvl, int skillId, int maxRemoved)
	{
		int count = maxRemoved <= 0 ? -2 : 0;
		for(L2Effect e : target.getAllEffects())
		{
			if(e == null)
			{
				continue;
			}

			if(negateLvl == -1) // if power is -1 the effect is always removed without power/lvl check ^^
			{
				if(e.getSkill().getSkillType() == type || e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type)
				{
					if(skillId != 0)
					{
						if(skillId == e.getSkill().getId() && count < maxRemoved)
						{
							e.exit();
							if(count > -1)
							{
								count++;
							}
						}
					}
					else if(count < maxRemoved)
					{
						e.exit();
						if(count > -1)
						{
							count++;
						}
					}
				}
			}
			else
			{
				boolean cancel = false;
				if(e.getSkill().getEffectType() != null && e.getSkill().getEffectAbnormalLvl() >= 0)
				{
					if(e.getSkill().getEffectType() == type && e.getSkill().getEffectAbnormalLvl() <= negateLvl)
					{
						cancel = true;
					}
				}
				else if(e.getSkill().getSkillType() == type && e.getSkill().getAbnormalLvl() <= negateLvl)
				{
					cancel = true;
				}

				if(cancel)
				{
					if(skillId != 0)
					{
						if(skillId == e.getSkill().getId() && count < maxRemoved)
						{
							e.exit();
							if(count > -1)
							{
								count++;
							}
						}
					}
					else if(count < maxRemoved)
					{
						e.exit();
						if(count > -1)
						{
							count++;
						}
					}
				}
			}
		}

		return maxRemoved <= 0 ? count + 2 : count;
	}

	private L2Effect[] SortEffects(L2Effect[] initial)
	{
		//this is just classic insert sort
		//If u can find better sort for max 20-30 units, rewrite this... :)
		int min;
		int index = 0;
		L2Effect pom;
		for(int i = 0; i < initial.length; i++)
		{
			min = initial[i].getSkill().getMagicLevel();
			for(int j = i; j < initial.length; j++)
			{
				if(initial[j].getSkill().getMagicLevel() <= min)
				{
					min = initial[j].getSkill().getMagicLevel();
					index = j;
				}
			}
			pom = initial[i];
			initial[i] = initial[index];
			initial[index] = pom;
		}

		return initial;
	}
}