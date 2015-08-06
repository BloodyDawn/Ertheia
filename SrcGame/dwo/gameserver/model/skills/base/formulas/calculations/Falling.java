package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 23:09
 */

public class Falling
{
	/**
	 * Calculate damage caused by falling
	 * @param cha
	 * @param fallHeight
	 * @return damage
	 */
	public static double calcFallDam(L2Character cha, int fallHeight)
	{
		if(!Config.ENABLE_FALLING_DAMAGE || fallHeight < 0)
		{
			return 0;
		}
		return cha.calcStat(Stats.FALL, fallHeight * cha.getMaxHp() / 1000, null, null);
	}
}
