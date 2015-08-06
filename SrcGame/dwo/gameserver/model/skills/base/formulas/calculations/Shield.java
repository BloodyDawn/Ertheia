package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:56
 */

public class Shield
{
	/**
	 * Returns:<br>
	 * 0 = shield defense doesn't succeed<br>
	 * 1 = shield defense succeed<br>
	 * 2 = perfect block<br>
	 *
	 * @param attacker
	 * @param target
	 * @param skill
	 * @param sendSysMsg
	 * @return
	 */
	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill, boolean sendSysMsg)
	{
		if(skill != null && skill.ignoreShield())
		{
			return 0;
		}

		L2Item item = target.getSecondaryWeaponItem();
		if(item == null || !(item instanceof L2Armor) || ((L2Armor) item).getItemType() == L2ArmorType.SIGIL)
		{
			return 0;
		}

		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.CON.calcBonus(target);
		if(shldRate == 0.0)
		{
			return 0;
		}

		int degreeside = (int) target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 0, null, null) + 120;
		if(degreeside < 360 && !target.isFacing(attacker, degreeside))
		{
			return 0;
		}

		byte shldSuccess = Variables.SHIELD_DEFENSE_FAILED;
		// if attacker
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		L2Weapon at_weapon = attacker.getActiveWeaponItem();
		if(at_weapon != null && at_weapon.getItemType() == L2WeaponType.BOW)
		{
			shldRate *= 1.3;
		}

		if(shldRate > 0 && Rnd.getChance(Config.ALT_PERFECT_SHLD_BLOCK))
		{
			shldSuccess = Variables.SHIELD_DEFENSE_PERFECT_BLOCK;
		}
		else if(Rnd.getChance(shldRate))
		{
			shldSuccess = Variables.SHIELD_DEFENSE_SUCCEED;
		}

		if(sendSysMsg && target instanceof L2PcInstance)
		{
			L2PcInstance enemy = (L2PcInstance) target;

			switch(shldSuccess)
			{
				case Variables.SHIELD_DEFENSE_SUCCEED:
					enemy.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
					break;
				case Variables.SHIELD_DEFENSE_PERFECT_BLOCK:
					enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
					break;
			}
		}

		if(target != null && (target.isDebug() || Config.DEVELOPER))
		{
			target.sendDebugMessage("calcShieldUse Rate: " + shldRate + " Success:" + shldSuccess);
		}
		return shldSuccess;
	}

	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill)
	{
		return calcShldUse(attacker, target, skill, true);
	}

	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, null, true);
	}
}
