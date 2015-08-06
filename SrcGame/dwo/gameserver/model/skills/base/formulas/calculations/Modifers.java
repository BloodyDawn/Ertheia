package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.skills.base.L2Skill;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:58
 */

public class Modifers
{
	public static double calcSkillStatMod(L2Skill skill, L2Character target)
	{
		return skill.getSaveVs() != null ? skill.getSaveVs().calcBonus(target) : 1;
	}

	public static double calcLvlBonusMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		int attackerLvl = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();
		double skillLvlBonusRateMod = 1 + skill.getLvlBonusRate() / 100.0;
		double lvlMod = 1 + (attackerLvl - target.getLevel()) / 100.0;
		return skillLvlBonusRateMod * lvlMod;
	}

	public static int calcElementModifier(L2Character attacker, L2Character target, L2Skill skill)
	{
		byte element = skill.getElement();

		if(element == Elementals.NONE)
		{
			return 0;
		}

		int result = skill.getElementPower();
		if(attacker.getAttackElement() == element)
		{
			result += attacker.getAttackElementValue(element);
		}

		result -= target.getDefenseElementValue(element);

		if(result < 0)
		{
			return 0;
		}

		return Math.round((float) result / 10);
	}
}
