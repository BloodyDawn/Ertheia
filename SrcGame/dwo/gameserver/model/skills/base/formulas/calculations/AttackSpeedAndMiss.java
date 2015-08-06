package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:48
 */

public class AttackSpeedAndMiss
{
	/** Calculate delay (in milliseconds) before next ATTACK
	 * @param attacker
	 * @param target
	 * @param rate
	 * @return
	 */
	public static int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if(rate < 2)
		{
			return 2700;
		}
		return (int) (470000 / rate);
	}

	/** Calculate delay (in milliseconds) for skills cast
	 * @param attacker
	 * @param skill
	 * @param skillTime
	 * @return
	 */
	public static int calcAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if(skill.isMagic())
		{
			return (int) (skillTime / attacker.getMAtkSpd() * 333);
		}
		return (int) (skillTime / attacker.getPAtkSpd() * 300);
	}

	/**
	 * Returns true if hit missed (target evaded)
	 *
	 * @param attacker
	 * @param target
	 * @return
	 */
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		boolean isBow = false;
		if(attacker.getActiveWeaponItem() != null && !attacker.isTransformed())
		{
			switch(attacker.getActiveWeaponItem().getItemType())
			{
				case BOW:
					isBow = true;
					break;
				case CROSSBOW:
				case TWOHANDCROSSBOW:
					isBow = true;
					break;
			}
		}

		// accuracy+dexterity => probability to hit in percents
		int acc_attacker = attacker.getPhysicalAccuracy();
		int evas_target = target.getPhysicalEvasionRate(attacker);

		double miss_chance = 10 * Math.pow(1.09, evas_target - acc_attacker);

		int a_dif = Math.abs(attacker.getHeading() - target.getHeading());

		if(a_dif > 8050 && a_dif < 24150 || a_dif > 40250 && a_dif < 56350)
		{
			// if(attacker instanceof L2PcInstance)
			// ((L2PcInstance)attacker).sendMessage("Side");
			miss_chance *= 0.95; // side = 5%
		}
		else if(a_dif <= 8050 || a_dif >= 56350)
		{
			// if(attacker instanceof L2PcInstance)
			// ((L2PcInstance)attacker).sendMessage("Back");
			miss_chance *= 0.9; // back = 10%
		}
		// else if(attacker instanceof L2PcInstance)
		// ((L2PcInstance)attacker).sendMessage("Front");
		if(attacker.getZ() > target.getZ())
		{
			miss_chance *= 0.97; // high = 3%
		}
		else if(attacker.getZ() < target.getZ())
		{
			miss_chance *= 1.03; // low = -3%
		}
		if(isBow)
		{
			int dx = attacker.getX() - target.getX();
			int dy = attacker.getY() - target.getY();
			int dz = attacker.getZ() - target.getZ();
			double dbl_range = dx * dx + dy * dy + dz * dz;
			miss_chance *= 1 + dbl_range / 3240000;// 3240000 == 1800^2 - nie pierwiastkujemy!
		}

		if(miss_chance > 800)
		{
			miss_chance = 800;
		}
		if(miss_chance < 1)
		{
			miss_chance = 1;
		}

		return Rnd.get(1000) < miss_chance;
	}
}
