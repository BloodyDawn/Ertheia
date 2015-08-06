package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 23:01
 */

public class Effects
{
	public static boolean calcEffectSuccess(L2Character attacker, L2Character target, EffectTemplate effect, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss)
	{
		return Skills.calcSkillSuccessAll(attacker, target, skill, shld, sps, bss, null, effect.effectPower, effect);
	}

	public static List<L2Effect> calcCancel(L2Character activeChar, L2Character target, L2Skill skill, double power)
	{
		int cancelMagicLvl = skill.getMagicLevel();
		int count = skill.getMaxNegatedEffects();
		double vuln = target.calcStat(Stats.CANCEL_VULN, 0, target, null);
		double prof = activeChar.calcStat(Stats.CANCEL_PROF, 0, target, null);
		double resMod = 1 + (vuln + prof) * -1 / 100;
		double rate = power / resMod;

		// Cancel for Abnormals.
		L2Effect[] effects = target.getAllEffects();
		List<L2Effect> canceled = new ArrayList<>(count);
		if(skill.getNegateAbnormals() != null)
		{
			for(L2Effect eff : effects)
			{
				if(eff == null)
				{
					continue;
				}

				skill.getNegateAbnormals().keySet().stream().filter(negateAbnormalType -> negateAbnormalType.equalsIgnoreCase(eff.getAbnormalType()) && skill.getNegateAbnormals().get(negateAbnormalType) >= eff.getAbnormalLvl()).forEach(negateAbnormalType -> {
					if(calcCancelSuccess(eff, cancelMagicLvl, (int) rate, skill, target))
					{
						eff.exit();
					}
				});
			}
		}
		// Common Cancel/Steal.
		else
		{
			// First Pass.
			int lastCanceledSkillId = 0;
			L2Effect effect;
			for(int i = effects.length; --i >= 0; ) // reverse order
			{
				effect = effects[i];
				if(effect == null)
				{
					continue;
				}

				// remove effect if can't be stolen
				if(!effect.canBeStolen())
				{
					effects[i] = null;
					continue;
				}

				// if effect time is smaller than 5 seconds, will not be stolen, just to save CPU,
				// avoid synchronization(?) problems and NPEs
				if(effect.getAbnormalTime() - effect.getTime() < 5)
				{
					effects[i] = null;
					continue;
				}

				// Only Dances/Songs.
				if(!effect.getSkill().isDance())
				{
					continue;
				}

				if(!calcCancelSuccess(effect, cancelMagicLvl, (int) rate, skill, target))
				{
					continue;
				}

				if(effect.getSkill().getId() != lastCanceledSkillId)
				{
					lastCanceledSkillId = effect.getSkill().getId();
					count--;
				}

				canceled.add(effect);
				if(count == 0)
				{
					break;
				}
			}
			// Second Pass.
			if(count > 0)
			{
				lastCanceledSkillId = 0;
				for(int i = effects.length; --i >= 0; )
				{
					effect = effects[i];
					if(effect == null)
					{
						continue;
					}

					// All Except Dances/Songs.
					if(effect.getSkill().isDance())
					{
						continue;
					}

					if(!calcCancelSuccess(effect, cancelMagicLvl, (int) rate, skill, target))
					{
						continue;
					}

					if(effect.getSkill().getId() != lastCanceledSkillId)
					{
						lastCanceledSkillId = effect.getSkill().getId();
						count--;
					}

					canceled.add(effect);
					if(count == 0)
					{
						break;
					}
				}
			}
		}
		return canceled;
	}

	private static boolean calcCancelSuccess(L2Effect eff, int cancelMagicLvl, int rate, L2Skill skill, L2Character target)
	{
		// На 86 steal_divinity уже не проходит ( 86 - 72 )
		// TODO Вывести формулу штрафа
		if(target.getLevel() - cancelMagicLvl > 13)
		{
			return false;
		}

		rate *= eff.getSkill().getMagicLevel() > 0 ? cancelMagicLvl / eff.getSkill().getMagicLevel() : 1;
		if(rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if(rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}

		return Rnd.get(100) < rate;
	}

	public static int calcEffectAbnormalTime(Env env, EffectTemplate template)
	{
		L2Character caster = env.getCharacter();
		L2Character target = env.getTarget();
		L2Skill skill = env.getSkill();
		int tempAbnormalTime = template.abnormalTime != 0 || skill == null ? template.abnormalTime : skill.isPassive() || skill.isToggle() ? -1 : template.abnormalTime;

		if(tempAbnormalTime > 0 && skill != null)
		{
			if(Config.ENABLE_MODIFY_SKILL_DURATION)
			{
				if(Config.SKILL_DURATION_LIST.containsKey(skill.getId()))
				{
					if(skill.getLevel() < 100)
					{
						tempAbnormalTime = Config.SKILL_DURATION_LIST.get(skill.getId());
					}
					else if(skill.getLevel() >= 100 && skill.getLevel() < 140)
					{
						tempAbnormalTime += Config.SKILL_DURATION_LIST.get(skill.getId());
					}
					else if(skill.getLevel() > 140)
					{
						tempAbnormalTime = Config.SKILL_DURATION_LIST.get(skill.getId());
					}
					if(Config.DEBUG)
					{
						Variables._log.log(Level.DEBUG, "*** Skill " + skill.getName() + " (" + skill.getLevel() + ") changed duration to " + tempAbnormalTime + " seconds.");
					}
				}
			}
			else if(skill.getBuffDuration() > 0)
			{
                tempAbnormalTime = skill.getBuffDuration() / 1000 / (template.getTotalTickCount() == 0 ? 1 : template.getTotalTickCount());
			}

			// Поддержка деленного на 2 времени действия настоек на саммонов при поднятии хозяином
			// Если скилл имеет эффект настойки и является бафом
			if(skill.isHerbEffect() && skill.getSkillType() == L2SkillType.BUFF)
			{
				if(target instanceof L2SummonInstance)
				{
					tempAbnormalTime /= 2;
				}
				else if(target.isPlayer() && !target.getPets().isEmpty())
				{
					for(L2Summon pet : target.getPets())
					{
						if(pet instanceof L2SummonInstance)
						{
							tempAbnormalTime /= 2;
							break;
						}
					}
				}
			}

			if(env.isSkillMastery())
			{
				tempAbnormalTime <<= 1;
			}
		}
		return tempAbnormalTime;
	}

}