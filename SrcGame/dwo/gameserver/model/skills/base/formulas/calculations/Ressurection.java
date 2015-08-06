package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.stats.BaseStats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 23:06
 */

public class Ressurection
{
	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, L2Character caster)
	{
		if(baseRestorePercent == 0 || baseRestorePercent == 100)
		{
			return baseRestorePercent;
		}

		double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
		if(restorePercent - baseRestorePercent > 20.0)
		{
			restorePercent += 20.0;
		}

		restorePercent = Math.max(restorePercent, baseRestorePercent);
		restorePercent = Math.min(restorePercent, 90.0);

		return restorePercent;
	}
}
