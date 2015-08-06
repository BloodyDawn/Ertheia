package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 23:08
 */

public class Reflect
{
	/**
	 * Calculate skill reflection according these three possibilities: <li>
	 * Reflect failed</li> <li>Mormal reflect (just effects). <U>Only possible
	 * for skilltypes: BUFF, REFLECT, HEAL_PERCENT, MANAHEAL_PERCENT, HOT,
	 * CPHOT, MPHOT</U></li> <li>vengEance reflect (100% damage reflected but
	 * damage is also dealt to actor). <U>This is only possible for skills with
	 * skilltype PDAM, BLOW, CHARGEDAM, MDAM or DEATHLINK</U></li> <br>
	 * <br>
	 *
	 * @param target
	 * @param skill
	 * @return SKILL_REFLECTED_FAILED, SKILL_REFLECT_SUCCEED or
	 *         SKILL_REFLECT_VENGEANCE
	 */
	public static byte calcSkillReflect(L2Character target, L2Skill skill)
	{
		/*
		 * Neither some special skills (like hero debuffs...) or those skills
		 * ignoring resistances can be reflected
		 */
		if(skill.ignoreResists() || !skill.canBeReflected())
		{
			return Variables.SKILL_REFLECT_FAILED;
		}

		// only magic and melee skills can be reflected
		if(!skill.isMagic() && (skill.getCastRange() == -1 || skill.getCastRange() > Variables.MELEE_ATTACK_RANGE))
		{
			return Variables.SKILL_REFLECT_FAILED;
		}

		byte reflect = Variables.SKILL_REFLECT_FAILED;
		// check for non-reflected skilltypes, need additional retail check
		switch(skill.getSkillType())
		{
			case BUFF:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
			case HOT:
			case CPHOT:
			case MPHOT:
			case UNDEAD_DEFENSE:
			case AGGDEBUFF:
			case CONT:
				return Variables.SKILL_REFLECT_FAILED;
			// these skill types can deal damage
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
				/* VENGEANCE_SKILL_MAGIC_DAMAGE & VENGEANCE_SKILL_PHYSICAL_DAMAGE отражают только определенные скиллы с определенным скилтайпом */
				Stats stat = skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE;
				double venganceChance = target.getStat().calcStat(stat, 0, target, skill);

				if(Rnd.getChance(venganceChance))
				{
					reflect |= Variables.SKILL_REFLECT_VENGEANCE;
				}
				break;
		}

		/* REFLECT_SKILL_MAGIC & REFLECT_SKILL_PHYSIC отражают обсалютно все маг и физ скиллы */
		Stats stat = skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC;
		double reflectChance = target.getStat().calcStat(stat, 0, null, skill);

		if(Rnd.getChance(reflectChance))
		{
			reflect |= Variables.SKILL_REFLECT_SUCCEED;
		}

		return reflect;
	}

	// 100% рефлект от SKILL_REFLECT_VENGEANCE ( конт атака )
	public static double getReflectDamageFromSkillVengeance(L2Character attacker, L2Character target, double damage, L2Skill skill)
	{
		return getDamage(attacker, target, (int) damage, 100.0, false);
	}

	// Рефлект от скилов PDAM и MDAM
	public static int getReflectDamageFromSkill(L2Character attacker, L2Character target, int damage, L2Skill skill)
	{
		if(skill != null)
		{
			double reflectPercent = 0;

			if(skill.isMagic())
			{
				reflectPercent = target.calcStat(Stats.REFLECT_SKILL_MAGIC_DAMAGE, 0, target, skill);
			}
			else if(skill.getCastRange() <= 200)
			{
				reflectPercent = target.calcStat(Stats.REFLECT_SKILL_PHYSIC_DAMAGE, 0, target, skill);
			}

			return getDamage(attacker, target, damage, reflectPercent, false);
		}

		return 0;
	}

	// Обычный рефлект
	public static int getReflectDamage(L2Character attacker, L2Character target, int damage)
	{
		return getDamage(attacker, target, damage, target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null), true);
	}

	private static int getDamage(L2Character attacker, L2Character target, int damage, double reflectPercent, boolean resist)
	{
		double reflectedDamage = 0;

		// Рефлект не работает в бессмертии
		if(!target.isInvul())
		{
			// Рефлект не работает на рейдов и если цель не выше на 8 уровней
			if(!target.isRaid() && attacker != null && attacker.getLevel() <= target.getLevel() + 8)
			{
				// Резист от рефлекта
				if(resist)
				{
					reflectPercent *= Math.max(attacker.getStat().calcStat(Stats.REFLECT_VULN, 100, null, null), 0) / 100;
				}

				if(reflectPercent > 0)
				{
					reflectedDamage = damage * (reflectPercent / 100.0);

					// Нельзя отрефлектить больше чем пдеф у цели
					if(reflectedDamage > target.getPDef(null))
					{
						reflectedDamage = target.getPDef(null);
					}

					// Нельзя отрефлектить больше чем хп у цели
					if(reflectedDamage > target.getMaxHp())
					{
						reflectedDamage = target.getMaxHp();
					}
				}

				// Дебаг для админа
				if(attacker.isDebug() || Config.DEVELOPER)
				{
					attacker.sendDebugMessage("reflectDamage: " + reflectedDamage + " percent: " + reflectPercent);
				}
			}
		}
		return (int) reflectedDamage;
	}

}
