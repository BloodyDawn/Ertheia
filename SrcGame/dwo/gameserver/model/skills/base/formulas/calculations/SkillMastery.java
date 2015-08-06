package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 23:07
 */

public class SkillMastery
{
	public static boolean calcSkillMastery(L2Character actor, L2Skill sk)
	{
		if(sk.getSkillType() == L2SkillType.FISHING)
		{
			return false;
		}

		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);

		if(actor instanceof L2PcInstance)
		{
			val *= ((L2PcInstance) actor).isMageClass() ? BaseStats.INT.calcBonus(actor) : BaseStats.STR.calcBonus(actor);
		}
		return Rnd.getChance(val);
	}
}
