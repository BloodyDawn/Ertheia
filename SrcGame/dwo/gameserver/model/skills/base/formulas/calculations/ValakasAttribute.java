package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TraitType;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:27
 */

public class ValakasAttribute
{
	public static double calcValakasTrait(L2Character attacker, L2Character target, L2Skill skill)
	{
		double calcPower = 0;
		double calcDefen = 0;

		if(skill != null && skill.getTraitType() == L2TraitType.VALAKAS)
		{
			calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
			calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
		}
		else
		{
			calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
			if(calcPower > 0)
			{
				calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
				calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
			}
		}
		return calcPower - calcDefen;

	}
}
