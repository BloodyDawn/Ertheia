package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:26
 */

public class Elemental
{
	public static double calcElemental(L2Character attacker, L2Character target, L2Skill skill)
	{
	    /* При атаке суммоном атака имеет тот же атрибут что и у игрока */
		if(attacker.isSummon())
		{
			attacker = attacker.getActingPlayer();
		}

		if(target.isSummon())
		{
			target = target.getActingPlayer();
		}

		int calcPower = 0;
		int calcDefen = 0;
		int calcTotal = 0;
		double result = 1.0;
		byte element;

		if(skill != null)
		{
			element = skill.getElement();
			if(element >= 0)
			{
				calcPower = skill.getElementPower();
				calcDefen = target.getDefenseElementValue(element);

				if(attacker.getAttackElement() == element)
				{
					calcPower += attacker.getAttackElementValue(element);
				}

				calcTotal = calcPower - calcDefen;
				if(calcTotal > 0)
				{
					if(calcTotal < 50)
					{
						result += calcTotal * 0.003948;
					}
					else if(calcTotal < 150)
					{
						result = 1.1974;
					}
					else
					{
						result = calcTotal < 300 ? 1.3973 : 1.6963;
					}
				}
			}
		}
		else
		{
			element = attacker.getAttackElement();
			if(element >= 0)
			{
				calcTotal = Math.max(attacker.getAttackElementValue(element) - target.getDefenseElementValue(element), 0);

				if(calcTotal < 50)
				{
					result += calcTotal * 0.003948;
				}
				else if(calcTotal < 150)
				{
					result = 1.1974;
				}
				else
				{
					result = calcTotal < 300 ? 1.3973 : 1.6963;
				}
			}
		}
		return result;
	}
}
