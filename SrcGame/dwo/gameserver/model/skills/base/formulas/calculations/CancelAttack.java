package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:48
 */

public class CancelAttack
{
	/**
	 * Формула отмены каста при атаке кастера.
	 */
	public static void calcAtkBreak(L2Character target, double dmg)
	{
		if(calc(target, dmg))
		{
			target.breakAttack();
			target.breakCast();
		}
	}

	private static boolean calc(L2Character target, double dmg)
	{
		if(target.isRaid() || !target.isCastingNow())
		{
			return false;
		}

		if(target.getFusionSkill() != null)
		{
			return true;
		}

		// 5 * 100% * dmg
		double chance = 500 * dmg / (target.getMaxHp() + target.getMaxCp());

		// учитываем резисты
		chance -= BaseStats.MEN.calcBonus(target) * 100 - 100;
		chance = target.calcStat(Stats.ATTACK_CANCEL, chance, null, null);

		if(chance > 99)
		{
			chance = 99;
		}
		if(chance < 1)
		{
			chance = 1;
		}

		return Rnd.get(100) < chance;
	}
}
