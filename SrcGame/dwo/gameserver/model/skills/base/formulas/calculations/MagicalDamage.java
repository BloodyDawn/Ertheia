package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:34
 */

public class MagicalDamage
{
	public static double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss, boolean bss, boolean mcrit)
	{
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);

		boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable;

		// AI SpiritShot
		if(attacker instanceof L2Npc)
		{
			ss = ((L2Npc) attacker)._spiritshotcharged;
			((L2Npc) attacker)._spiritshotcharged = false;
		}
		// --------------------------------
		// Pvp bonuses for def
		if(isPvP)
		{
			mDef *= skill.isMagic() ? target.calcStat(Stats.PVP_MAGICAL_DEF, 1, null, null) : target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}

		switch(shld)
		{
			case Variables.SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef(); // kamael
				break;
			case Variables.SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}

		if(bss)
		{
			mAtk *= 4;
		}
		else if(ss)
		{
			mAtk *= 2;
		}

		// MDAM Formula.
		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker, target, isPvP, isPvE);

		// Failure calculation
		if(Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if(attacker instanceof L2PcInstance)
			{
				if(calcMagicSuccess(attacker, target, skill) && target.getLevel() - attacker.getLevel() <= 9)
				{
					if(skill.getSkillType() == L2SkillType.DRAIN)
					{
						attacker.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
					}
					else
					{
						attacker.sendPacket(SystemMessageId.ATTACK_FAILED);
					}

					damage /= 2;
				}
				else
				{
					attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					damage = 1;
				}
			}

			if(target instanceof L2PcInstance)
			{
				if(skill.getSkillType() == L2SkillType.DRAIN)
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN).addCharName(attacker));
				}
				else
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC).addCharName(attacker));
				}
			}
		}
		else if(mcrit)
		{
			if(attacker instanceof L2PcInstance && target instanceof L2PcInstance)
			{
				// Возможно для перерожденных множитель крита меньше чем для не перерожденных
				damage *= attacker.isAwakened() ? 1.5 : 2.5;
			}
			else
			{
				damage *= 2;
			}

			// Для формулы если в скилле используется mul
			damage *= attacker.calcStat(Stats.MAGIC_CRIT_DMG, 1, null, null);

			// Для формулы если в скилле используется просто прибавка к дамагу
			damage += attacker.calcStat(Stats.MAGIC_CRIT_DMG_ADD, 0, null, null);

			// Множим на % резиста от магического крита цели (например 1000 крит * 0.5(50%) резиста = 500 итоговый крит)
			damage *= target.calcStat(Stats.MAGIC_CRIT_VULN, target.getTemplate().getBaseMCritVuln(), target, null);
		}

		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();

		if(target instanceof L2Npc)
		{
			// Для всех нпц
			damage *= attacker.getMAtkNpcs(target);

			switch(((L2Npc) target).getTemplate().getRace())
			{
				case BEAST:
					damage *= attacker.getMAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getMAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getMAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getMAtkDragons(target);
					break;
				case BUG:
					damage *= attacker.getMAtkInsects(target);
					break;
				case GIANT:
					damage *= attacker.getMAtkGiants(target);
					break;
				case MAGICCREATURE:
					damage *= attacker.getMAtkMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		// Pvp bonuses for dmg
		if(isPvP)
		{
			damage *= skill.isMagic() ? attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null) : attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
		}

		// CT2.3 general magic vuln
		damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1, null, null);

		damage *= Elemental.calcElemental(attacker, target, skill);

		if(target instanceof L2Attackable)
		{
			damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if(!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY && attacker.getActingPlayer() != null && target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				damage *= lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size() ? Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size()) : Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
		}
		return damage;
	}

	public static double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit, byte shld)
	{
		int mAtk = attacker.getCubicPower();
		int mDef = target.getMDef(attacker.getOwner(), skill);

		// Current info include mAtk in the skill power.
		boolean isPvP = target instanceof L2Playable;
		boolean isPvE = target instanceof L2Attackable;

		switch(shld)
		{
			case Variables.SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef(); // kamael
				break;
			case Variables.SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}

		// Cubics MDAM Formula (similar to PDAM formula, but using 91 instead of 70, also resisted by mDef).
		double damage = 91 * ((mAtk + skill.getPower(isPvP, isPvE)) / mDef);

		L2PcInstance owner = attacker.getOwner();
		// Failure calculation
		if(Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill))
		{
			if(calcMagicSuccess(owner, target, skill) && target.getLevel() - skill.getMagicLevel() <= 9)
			{
				if(skill.getSkillType() == L2SkillType.DRAIN)
				{
					owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
				}
				else
				{
					owner.sendPacket(SystemMessageId.ATTACK_FAILED);
				}

				damage /= 2;
			}
			else
			{
				owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
				damage = 1;
			}

			if(target instanceof L2PcInstance)
			{
				if(skill.getSkillType() == L2SkillType.DRAIN)
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN).addCharName(owner));
				}
				else
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC).addCharName(owner));
				}
			}
		}
		else if(mcrit)
		{
			damage *= 2; // В годе крит х2
			// Множим на % резиста от магического крита цели (например 1000 крит * 0.5(50%) резиста = 500 итоговый крит)
			damage *= target.calcStat(Stats.MAGIC_CRIT_VULN, target.getTemplate().getBaseMCritVuln(), target, null);
		}

		// CT2.3 general magic vuln
		damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1, null, null);

		damage *= Elemental.calcElemental(owner, target, skill);

		if(target instanceof L2Attackable)
		{
			damage *= attacker.getOwner().calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if(!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY && attacker.getOwner() != null && target.getLevel() - attacker.getOwner().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getOwner().getLevel() - 1;
				damage *= lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size() ? Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size()) : Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
		}
		return damage;
	}

	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		if(skill.getPower() == -1)
		{
			return true;
		}

		// DS: remove skill magic level dependence from nukes
		// int lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ?
		// skill.getMagicLevel() : attacker.getLevel()));
		int lvlDifference = target.getLevel() - (skill.getSkillType() == L2SkillType.SPOIL ? skill.getMagicLevel() : attacker.getLevel());
		double lvlModifier = Math.pow(1.3, lvlDifference);
		float targetModifier = 1;
		if(target instanceof L2Attackable && !target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY && attacker.getActingPlayer() != null && target.getLevel() - attacker.getActingPlayer().getLevel() >= 3)
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 2;
			targetModifier = lvlDiff > Config.NPC_SKILL_CHANCE_PENALTY.size() ? Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size()) : Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
		}
		// general magic resist
		double resModifier = target.calcStat(Stats.MAGIC_SUCCESS_RES, 1, null, skill);
		double failureModifier = attacker.calcStat(Stats.MAGIC_FAILURE_RATE, 1, target, skill);

		// TODO: Подправить формулу магического уклонения
		int acc_attacker = attacker.getMagicalAccuracy();
		int evas_target = target.getMagicalEvasionRate(attacker);
		double miss_chance = Math.pow(1.059, evas_target - acc_attacker);
		miss_chance = Math.min(80, Math.max(1, miss_chance));

		int rate = 100 - Math.round((float) (lvlModifier * targetModifier * resModifier * failureModifier * miss_chance));

		if(rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if(rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}

		if(attacker.isDebug() || Config.DEVELOPER)
		{
			StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " lvlDiff:", String.valueOf(lvlDifference), " lvlMod:", String.format("%1.2f", lvlModifier), " res:", String.format("%1.2f", resModifier), " fail:", String.format("%1.2f", failureModifier), " tgt:", String.valueOf(targetModifier), " total:", String.valueOf(rate));
			String result = stat.toString();
			if(attacker.isDebug())
			{
				attacker.sendDebugMessage(result);
			}
		}
		return Rnd.getChance(rate);
	}

	public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		double defence = 0;
		if(skill.isActive() && skill.isOffensive() && !skill.isNeutral())
		{
			defence = target.getMDef(actor, skill);
		}

		double attack = 2 * actor.getMAtk(target, skill) * (1 + ProficiencyAndVuln.calcSkillVulnerability(actor, target, skill) / 100);
		double d = (attack - defence) / (attack + defence);

		if(target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0 && skill.isDebuff() && !skill.ignoreResists())
		{
			L2Effect eff = target.getFirstEffect(skill);
			if(eff != null && eff.getRemovedEffectType().contains(L2EffectStopCond.ON_START_DEBUFF) && eff.isHitCountRemove())
			{
				eff.exit();
			}
			else
			{
				return false;
			}
		}

		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}

	public static boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}
}
