package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:21
 */

public class BlowDamage implements Variables
{
	public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss)
	{
		double defence = target.getPDef(attacker);

		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
			{
				defence += target.getShldDef();
				break;
			}
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
			{
				return 1;
			}
		}

		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isAttackable();
		double power = skill.getPower(isPvP, isPvE);
		double damage = 0;
		double proximityBonus = attacker.isBehindTarget() ? 1.2 : attacker.isInFrontOfTarget() ? 1 : 1.1; // Behind: +20% - Side: +10% (TODO: values are unconfirmed, possibly custom, remove or update when confirmed);
		double ssboost = ss ? 2 : 1;
		double pvpBonus = 1;

		if (isPvP)
		{
			// Damage bonuses in PvP fight
			pvpBonus = attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			// Defense bonuses in PvP fight
			defence *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}

		// Initial damage
		double baseMod = ((77 * (power + (attacker.getPAtk(target) * ssboost))) / defence);

		// Critical
		double criticalMod = (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill));
		double criticalVulnMod = (target.calcStat(Stats.CRIT_VULN, 1, target, skill));
		double criticalAddMod = ((attacker.getStat().calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 77) / defence);
		double criticalAddVuln = target.calcStat(Stats.CRIT_ADD_VULN, 0, target, skill);

		// Trait, elements
		//double generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
		double attributeMod = Elemental.calcElemental(attacker, target, skill);
		double weaponMod = attacker.getRandomDamageMultiplier();

		double penaltyMod = 1;
		if ((target instanceof L2Attackable) && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
			if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
			{
				penaltyMod *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
			}
			else
			{
				penaltyMod *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
		}

		damage = (baseMod * criticalMod * criticalVulnMod * proximityBonus * pvpBonus) + criticalAddMod + criticalAddVuln;
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage += ValakasAttribute.calcValakasTrait(attacker, target, skill);
		// damage *= generalTraitMod;
		damage *= attributeMod;
		damage *= weaponMod;
		damage *= penaltyMod;

		if (isPvP)
		{
			// Dmg bonusses in PvP fight
			damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			// Def bonusses in PvP fight
			damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}

		return Math.max(damage, 1);
	}

	/**
	 * Calculates blow success depending on base chance and relative position of attacker and target
	 * @param activeChar Target that is performing skill
	 * @param target Target of this skill
	 * @param skill Skill which will be used to get base value of blowChance and crit condition
	 * @return Success of blow
	 */
	public static boolean calcBlowSuccess(L2Character activeChar, L2Character target, L2Skill skill)
	{
		int blowChance = skill.getBlowChance();

		// Skill is blow and it has 0% to make dmg... thats just wrong
		if(blowChance == 0)
		{
			//_log.log(Level.WARN, "Skill " + skill.getId() + " - " + skill.getName() + " has 0 blow land chance, yet its a blow skill!");
			//TODO: return false;
			//lets add 20 for now, till all skills are corrected
			blowChance = 20;
		}

		switch(activeChar.getTargetPosition(target))
		{
			case BACK:
				blowChance *= 1.8; //double chance from behind
				break;
			case FRONT:
				if((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
				{
					return false;
				}
				break;
			case SIDE:
				blowChance *= 1.5; //50% better chance from side
				break;
		}

		return Rnd.getChance(activeChar.calcStat(Stats.BLOW_RATE, blowChance * (1.0 + activeChar.getDEX() / 100.0), target, null));
	}

	/** Calculate value of lethal chance */
	public static double calcLethal(L2Character activeChar, L2Character target, int baseLethal, int magiclvl)
	{
		double chance = 0;
		if(magiclvl > 0)
		{
			int delta = (magiclvl + activeChar.getLevel()) / 2 - 1 - target.getLevel();

			// delta [-3,infinite)
			if(delta >= -3)
			{
				chance = baseLethal * (double) activeChar.getLevel() / target.getLevel();
			}
			// delta [-9, -3[
			else
			{
				chance = delta < -3 && delta >= -9 ? -3 * (baseLethal / delta) : baseLethal / 15;
			}
		}
		else
		{
			chance = baseLethal * (double) activeChar.getLevel() / target.getLevel();
		}
		return 10 * activeChar.calcStat(Stats.LETHAL_RATE, chance, target, null);
	}

	public static boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if(target.isLethalable() && !(target instanceof L2Npc && ((L2Npc) target).getNpcId() == 35062)) // TODO: Unhardcode
		{
			if(skill.getLethalChance2() > 0 && Rnd.get(1000) < calcLethal(activeChar, target, skill.getLethalChance2(), skill.getMagicLevel()))
			{
				if(target.isNpc())
				{
					target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar, skill);
				}
				else if(target.isPlayer()) // If is a active player set his HP and CP to 1
				{
					L2PcInstance player = target.getActingPlayer();
					if(!player.isInvul())
					{
						player.setCurrentHp(1);
						player.setCurrentCp(1);
						player.sendPacket(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL);
					}
				}
				activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE);
			}
			else if(skill.getLethalChance1() > 0 && Rnd.get(1000) < calcLethal(activeChar, target, skill.getLethalChance1(), skill.getMagicLevel()))
			{
				// Yes ! mobs can get half-killed as well!
				if(target.isMonster())
				{
					target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar, skill);
					activeChar.sendPacket(SystemMessageId.HALF_KILL);
				}
				else if(target.isPlayer())
				{
					L2PcInstance player = target.getActingPlayer();
					if(!player.isInvul())
					{
						player.setCurrentCp(1); // Set CP to 1
						player.sendPacket(SystemMessageId.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
						activeChar.sendPacket(SystemMessageId.HALF_KILL);
					}
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		return true;
	}
}