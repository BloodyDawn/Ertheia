package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.handler.effects.ResistSkillId;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.proptypes.L2BasicResistType;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 23:03
 */

public class Skills
{
	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss)
	{
		return calcSkillSuccessAll(attacker, target, skill, shld, sps, bss, skill.getSkillType(), skill.getPower(), null);
	}

	public static boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill, byte shld)
	{
		return calcSkillSuccessAll(attacker.getOwner(), target, skill, shld, false, false, skill.getSkillType(), skill.getPower(), null);
	}

	/**
	 * Each debuff might be "perfect" resisted by DEBUFF_VULN stat.
	 * This is confirmed on retail
	 * @param attacker Attacking character
	 * @param target Char that might resist
	 * @return True if target resisted, false it target failed to resist
	 */
	public static boolean checkPerfectDebuffResistSuccess(L2Character attacker, L2Character target)
	{
		int debuffVuln = (int) (target.calcStat(Stats.DEBUFF_VULN, 0, target, null) * -0.5);  // Режим пока на половину    TODO уточнить

		if(debuffVuln > 0)
		{
			if(Rnd.getChance(debuffVuln))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean calcSkillSuccessAll(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean sps, boolean bss, L2SkillType effectType, double baseRate, EffectTemplate sEffect)
	{
		// Имунитет к дебафам
		if(skill.isDebuff() && target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
		{
			L2Effect eff = target.getFirstEffect(skill);
			if(eff != null && eff.getRemovedEffectType().contains(L2EffectStopCond.ON_START_DEBUFF) && eff.isHitCountRemove())
			{
				eff.exit();
			}
			else
			{
				return false;
			}
		}

		// Если повер -1 то шанс 100%
		if(baseRate == -1)
		{
			return true;
		}

		// Если effectType нету то идем в обход всех резистов TODO понять зачем ??
		if(effectType == null)
		{
			if(attacker.isDebug() || Config.DEVELOPER)
			{
				StringBuilder stat = new StringBuilder(100);

				if(sEffect != null)
				{
					StringUtil.append(stat, skill.getName(), " type:", skill.getSkillType().toString(), "[", sEffect.funcName, "]", " chance:", String.valueOf(baseRate));
				}
				else
				{
					StringUtil.append(stat, skill.getName(), " type:", skill.getSkillType().toString(), " chance:", String.valueOf(baseRate));
				}

				if(attacker.isDebug())
				{
					attacker.sendDebugMessage(stat.toString());
				}
			}
			return Rnd.getChance(baseRate);
		}

		// Скиллы которые не действуют на Рейд Боссов и нпц.
		if(target.isRaid() || target instanceof L2Npc && !(target instanceof L2Attackable))
		{
			switch(effectType)
			{
				case CONFUSION:
				case ROOT:
				case STUN:
				case MUTE:
				case FEAR:
					// case DEBUFF:
				case PARALYZE:
				case SLEEP:
				case AGGDEBUFF:
				case FLY_UP:
				case KNOCK_BACK:
				case KNOCK_DOWN:
					return false;
			}
		}

		// Cancel-эффекты проходят всегда	TODO разве ?
		if(effectType == L2SkillType.CANCEL)
		{
			return true;
		}

		// Поулчаем magicLevel скила
		int magicLevel = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();

		// Получаем модификатор уровня
		int lvlmodifier = (magicLevel - target.getLevel()) * skill.getLvlBonusRate();

		// Получаем разницу между игроком и дебафом
		int lvlDifference = target.getLevel() - magicLevel;

		if(skill.isDebuff())
		{
			// Если "превосходная блокировка щитом" то дебаф не проходит
			if(shld == Variables.SHIELD_DEFENSE_PERFECT_BLOCK)
			{
				return false;
			}

			// Есть шанс что дебаф не пройдет независимо от спец резиста.
			if(checkPerfectDebuffResistSuccess(attacker, target))
			{
				return false;
			}

			// заглушка ( пока временная )
			if(skill.getTraitType() != null && lvlDifference > 10)
			{
				return false;
			}

			// Если эффект отразился он не проходит   TODO 2 раза считать не верно
			//if (Reflect.calcSkillReflect(target, skill) != SKILL_REFLECT_FAILED)
			//	return false;

			boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
			boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable;

			// Если скилл игнорирует резисты.
			if(skill.ignoreResists())
			{
				return Rnd.getChance(skill.getPower(isPvP, isPvE));
			}

			// Резисты по skillId
			if(target.isAffected(CharEffectList.EFFECT_FLAG_RESIST_SKILLID))
			{
				for(L2Effect effect : target.getEffects(L2EffectType.RESIST_SKILL_ID))
				{
					ResistSkillId e = (ResistSkillId) effect;
					if(e.isResitedBySkillId(skill.getId()))
					{
						return false;
					}
				}
			}
		}

		// для вывода в дебаг
		double base = baseRate;

		// Подсчет резиста физ дебафов
		if(skill.getBasicProperty() == L2BasicResistType.PHYSIC)
		{
			baseRate += 10 * BaseStats.MEN.calcBonus(attacker);   // TODO сделать параметр
		}

		// Подсчет резиста маг дебафов
		if(skill.getBasicProperty() == L2BasicResistType.MAGIC)
		{
			baseRate += 10 * BaseStats.CON.calcBonus(attacker);   // TODO сделать параметр
		}

		baseRate += lvlmodifier;

		// TODO скорей всего выпилили
		// double statMod = Modifers.calcSkillStatMod(skill, target);

		// Резисты.
		double vuln = ProficiencyAndVuln.calcSkillVulnerability(attacker, target, skill);
		double prof = ProficiencyAndVuln.calcSkillProficiency(skill, attacker, target);
		double resMod = 1 + (vuln + prof) / 100;

		// Лимиты на резисты
		if(resMod > 1.9)
		{
			resMod = 1.9;
		}
		if(resMod < 0.1)
		{
			resMod = 0.1;
		}

		baseRate *= resMod;

		// Доболвение бонуса от атт
		int elementModifier = Modifers.calcElementModifier(attacker, target, skill);
		baseRate += elementModifier;

		// Доболвение бонуса от м Атаки
		double mAtkModifier = 0;
		int ssModifier = 1;
		if(skill.isMagic())
		{
			// Получаем мдеф таргета
			mAtkModifier = target.getMDef(target, skill);

			// Учитываем защиту щитом
			if(shld == Variables.SHIELD_DEFENSE_SUCCEED)
			{
				mAtkModifier += target.getShldDef();
			}

			// бонус от  Sps/SS
			if(bss)
			{
				ssModifier = 4;
			}
			if(sps)
			{
				ssModifier = 2;
			}

			// Получаем бонус от м атаки
			mAtkModifier = Math.sqrt(13.5 * Math.sqrt(ssModifier * attacker.getMAtk(target, skill)) / mAtkModifier);

			// TODO пересмотреть для кубиков
			//	mAtkModifier = Math.pow(attacker.getCubicPower() / mAtkModifier, 0.2);
			//	rate += (int) (mAtkModifier * 100) - 100;

			// Лимиты на бонус м атаки
			if(mAtkModifier < 0.7)
			{
				mAtkModifier = 0.7;
			}
			if(mAtkModifier > 1.4)
			{
				mAtkModifier = 1.4;
			}

			baseRate *= mAtkModifier;
		}

		// Проверяем на лимиты. Если разница больше 10 уровней то мин шанс не выставляем.
		if(baseRate > skill.getMaxChance())
		{
			baseRate = skill.getMaxChance();
		}
		if(baseRate < skill.getMinChance() && lvlDifference < 10)
		{
			baseRate = skill.getMinChance();
		}

		// Дебаг для админа
		if(attacker.isDebug() || Config.DEVELOPER)
		{
			StringBuilder stat = new StringBuilder(100);

			StringUtil.append(stat, skill.getName(), " type:", skill.getSkillType().toString(), " eff:", skill.getSkillType().toString(),

				" power:", String.valueOf(base), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", prof), "/", String.format("%1.2f", vuln), ") elem:", String.valueOf(elementModifier), " mAtk:", String.format("%1.2f", mAtkModifier), " lvl:", String.valueOf(lvlmodifier), " total:", String.valueOf(baseRate));
			String result = stat.toString();
			if(attacker.isDebug())
			{
				attacker.sendDebugMessage(result);
			}
		}
		return Rnd.getChance(baseRate);
	}
}
