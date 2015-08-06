package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.engine.logengine.formatters.DamageLogFormatter;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: ANZO, Bacek
 * Date: 29.09.11
 * Time: 22:24
 */

public class PhysicalDamage
{
	public static void physDamEffect(L2Character activeChar, L2Character target, L2Skill skill, byte shld, double damage, double modifier, Logger _logDamage)
	{
		boolean crit = false;
		if(skill.getBaseCritRate() > 0 && !skill.isStaticDamage())
		{
			crit = calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar), true, target);
		}

		if(!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
		{
			damage = 0;
		}

		if(!skill.isStaticDamage() && skill.getMaxSoulConsumeCount() > 0 && activeChar.isPlayer())
		{
			// Souls Formula (each soul increase +4%)
			damage *= activeChar.getActingPlayer().getSouls() * 0.04 + 1;
		}

		if(crit)
		{
			damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
		}

		int finalDamage = (int) (skill.isStaticDamage() ? damage : damage * modifier);

		boolean skillIsEvaded = calcPhysicalSkillEvasion(target, skill);
		byte reflect = Reflect.calcSkillReflect(target, skill);

		if(skillIsEvaded)
		{
			if(activeChar.isPlayer())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK).addString(target.getName()));
			}
			if(target.isPlayer())
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK).addString(activeChar.getName()));
			}

			// Possibility of a lethal strike despite skill is evaded
			BlowDamage.calcLethalHit(activeChar, target, skill);
		}
		else
		{
			if(skill.hasEffects())
			{
				L2Effect[] effects;
				if((reflect & Variables.SKILL_REFLECT_SUCCEED) == 0)
				{
					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, false, false, false))
					{
						target.stopSkillEffects(skill.getId());
						effects = skill.getEffects(activeChar, target, new Env(shld, false, false, false));
						if(effects != null && effects.length > 0)
						{
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
				}
				else
				{
					activeChar.stopSkillEffects(skill.getId());
					effects = skill.getEffects(target, activeChar);
					if(effects != null && effects.length > 0)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
				}
			}

			if(finalDamage > 0)
			{

				// В годе обычный рефлект так же распростроняется на физ скилы
				int reflectedDamage = Reflect.getReflectDamage(activeChar, target, finalDamage);
				if(reflectedDamage > 0)
				{
					// Шлем сообщение
					target.sendDamageMessage(activeChar, reflectedDamage, false, false, false);
					// Наносим урон
					activeChar.reduceCurrentHp(reflectedDamage, target, true, false, null);
				}

				// Отдельный рефлект от скилов
				reflectedDamage = Reflect.getReflectDamageFromSkill(activeChar, target, finalDamage, skill);
				if(reflectedDamage > 0)
				{
					// Шлем сообщение
					target.sendDamageMessage(activeChar, reflectedDamage, false, false, false);
					// Наносим урон
					activeChar.reduceCurrentHp(reflectedDamage, target, true, false, null);
				}

				// 100% Рефлект от скилов ( контр атака )
				if((reflect & Variables.SKILL_REFLECT_VENGEANCE) != 0)
				{
					if(target.isPlayer())
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK).addCharName(activeChar));
					}
					if(activeChar.isPlayer())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK).addCharName(target));
					}
					activeChar.reduceCurrentHp(Reflect.getReflectDamageFromSkillVengeance(activeChar, target, finalDamage, skill), target, skill);
				}

				// Ограничение урона
				int targetMaxSkillDamage = (int) target.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null);
				if(targetMaxSkillDamage > 0 && finalDamage > targetMaxSkillDamage)
				{
					finalDamage = targetMaxSkillDamage;
				}

				// Отсылаем сообщение
				activeChar.sendDamageMessage(target, finalDamage, false, crit, false);

				// Записываем в лог урон
				if(Config.LOG_GAME_DAMAGE && activeChar instanceof L2Playable && finalDamage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					_logDamage.log(Level.INFO, DamageLogFormatter.format("DAMAGE PDAM:", new Object[]{
						activeChar, " did damage ", finalDamage, skill, " to ", target
					}));
				}

				// Possibility of a lethal strike
				BlowDamage.calcLethalHit(activeChar, target, skill);

				// Наносим урон
				target.reduceCurrentHp(finalDamage, activeChar, skill);
			}
			else // No damage
			{
				activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
			}
		}

		// Считаем камаелькам восстановление душ
		if(activeChar.isPlayer())
		{
			L2PcInstance player = activeChar.getActingPlayer();
			int soulMastery = (int) activeChar.getStat().calcStat(Stats.SOUL_MASTERY, 0, null, null);
			if(soulMastery > 0)
			{
				if(player.getSouls() < soulMastery)
				{
					int count;

					count = player.getSouls() + skill.getNumSouls() <= soulMastery ? skill.getNumSouls() : soulMastery - player.getSouls();
					player.increaseSouls(count);
				}
				else
				{
					player.sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
				}
			}
		}

		//self Effect :]
		if(skill.hasSelfEffects())
		{
			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if(effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
	}

	/**
	 * Calculated damage caused by ATTACK of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 *
	 * @param attacker
	 *            player or NPC that makes ATTACK
	 * @param target
	 *            player or NPC, target of ATTACK
	 * @param shld
	 *            one of ATTACK_XXX constants
	 * @param crit
	 *            if the ATTACK have critical success
	 * @param dual
	 *            if dual weapon is used
	 * @param ss
	 *            if weapon item was charged by soulshot
	 * @return damage points
	 */
	public static double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean crit, boolean dual, boolean ss)
	{
		boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable;
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);

		damage += ValakasAttribute.calcValakasTrait(attacker, target, skill);

		if(attacker instanceof L2Npc)
		{
			ss = ((L2Npc) attacker)._soulshotcharged;
			((L2Npc) attacker)._soulshotcharged = false;
		}

		// Def bonusses in PvP fight
		if(isPvP)
		{
			defence *= target.calcStat(skill == null ? Stats.PVP_PHYSICAL_DEF : Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}

		switch(shld)
		{
			case Variables.SHIELD_DEFENSE_SUCCEED:
				defence += target.getShldDef();
				break;
			case Variables.SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1.0;
		}

		if(skill != null)
		{
			// Игнорирование процента физической защиты цели
			if(skill.getIgnorePdefPercent() > 0)
			{
				defence -= defence * skill.getIgnorePdefPercent() / 100;
			}

			damage += skill.getPower(attacker, target, isPvP, isPvE);
		}

		damage *= ss ? 2.04 : 1;

		if(target instanceof L2Npc)
		{
			// Для всех нпц
			defence /= attacker.getPDefNpcs(target);

			switch(((L2Npc) target).getTemplate().getRace())
			{
				case BEAST:
					defence /= attacker.getPDefMonsters(target);
					break;
				case ANIMAL:
					defence /= attacker.getPDefAnimals(target);
					break;
				case PLANT:
					defence /= attacker.getPDefPlants(target);
					break;
				case DRAGON:
					defence /= attacker.getPDefDragons(target);
					break;
				case BUG:
					defence /= attacker.getPDefInsects(target);
					break;
				case GIANT:
					defence /= attacker.getPDefGiants(target);
					break;
				case MAGICCREATURE:
					defence /= attacker.getPDefMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		boolean isBow = false;

		if(weapon != null && !attacker.isTransformed())
		{
			switch(weapon.getItemType())
			{
				case BOW:
					isBow = true;
					stat = Stats.BOW_WPN_VULN;
					break;
				case CROSSBOW:
				case TWOHANDCROSSBOW:
					isBow = true;
					stat = Stats.CROSSBOW_WPN_VULN;
					break;
				case BLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				case BIGBLUNT:
					stat = Stats.BIGBLUNT_WPN_VULN;
					break;
				case DUALDAGGER:
					stat = Stats.DUALDAGGER_WPN_VULN;
					break;
				case RAPIER:
					stat = Stats.RAPIER_WPN_VULN;
					break;
				case ANCIENTSWORD:
					stat = Stats.ANCIENT_WPN_VULN;
					break;
				case DUALBLUNT:
					stat = Stats.DUAL_BLUNT_WPN_VULN;
					break;
				/*case PET:
					stat = Stats.PET_WPN_VULN;
					break;*/
			}
		}

		if(target instanceof L2Npc)
		{
			// Для всех нпц
			defence /= attacker.getPAtkNpcs(target);

			switch(((L2Npc) target).getTemplate().getRace())
			{
				case BEAST:
					damage *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					damage *= attacker.getPAtkGiants(target);
					break;
				case MAGICCREATURE:
					damage *= attacker.getPAtkMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		// for summon use pet weapon vuln, since they cant hold weapon
		if(attacker instanceof L2SummonInstance)
		{
			stat = Stats.PET_WPN_VULN;
		}

		// Бонус урона от скилов
		if(skill != null)
		{
			/* Для формулы если в скилле используется mul */
			damage *= attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, 1, null, null);
			/* Для формулы если в скилле используется просто прибавка к дамагу */
			damage += attacker.calcStat(Stats.PHYSICAL_SKILL_POWER_ADD, 0, null, null);
		}

		if(crit)
		{
			if(skill != null)
			{
				damage *= 2;
			}
			else
			{
				// Крит у луков 20% / у остальных 100%
				damage *= isBow ? 1.2 : 2;
				damage *= attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill);
				damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill);
			}

			damage *= target.calcStat(Stats.CRIT_VULN, target.getTemplate().getBaseCritVuln(), target, null);
			damage += attacker.calcStat(Stats.CRIT_ADD_VULN, 0, target, skill);
		}

		// У лучников урон в 2 раза больше
		if(isBow)
		{
			damage *= 2;
		}

		// Верная формула по год
		damage = 70 * damage / defence;

		if(stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
		}

		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();

		// Лимит
		damage = Math.max(0, damage);

		/* Подсчет атт */
		damage *= Elemental.calcElemental(attacker, target, skill);

		// Dmg bonusses in PvP fight
		if(isPvP)
		{
			if(skill == null)
			{
				/* Для формулы если в скилле используется mul */
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
				/* Для формулы если в скилле используется просто прибавка к дамагу */
				damage += attacker.calcStat(Stats.PVP_PHYSICAL_DMG_ADD, 0, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}

		if(target instanceof L2Attackable)
		{
			if(isBow)
			{
				damage *= skill != null ? attacker.calcStat(Stats.PVE_BOW_SKILL_DMG, 1, null, null) : attacker.calcStat(Stats.PVE_BOW_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVE_PHYSICAL_DMG, 1, null, null);
			}

			// Штрафы на урон
			if(!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY && attacker.getActingPlayer() != null && target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if(skill != null)
				{
					damage *= lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size() ? Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size()) : Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
				else if(crit)
				{
					damage *= lvlDiff >= Config.NPC_CRIT_DMG_PENALTY.size() ? Config.NPC_CRIT_DMG_PENALTY.get(Config.NPC_CRIT_DMG_PENALTY.size()) : Config.NPC_CRIT_DMG_PENALTY.get(lvlDiff);
				}
				else
				{
					damage *= lvlDiff >= Config.NPC_DMG_PENALTY.size() ? Config.NPC_DMG_PENALTY.get(Config.NPC_DMG_PENALTY.size()) : Config.NPC_DMG_PENALTY.get(lvlDiff);
				}
			}
		}

		/* При обычной атаке есть шанс сбить состояние стана на таргете, при крите шанс сбить выше.  */
		if(target.isStunned() && Rnd.getChance(crit ? 75 : 10))
		{
			target.stopStunning(true);
		}

		CancelAttack.calcAtkBreak(target, damage);

		return damage;
	}

	/** Returns true in case of critical hit */
	public static boolean calcCrit(double rate, boolean skill, L2Character target)
	{
		boolean success = rate > Rnd.get(1000);

		// support for critical damage evasion
		if(success)
		{
			if(target == null)
			{
				return true; // no effect
			}

			return skill ? success : Rnd.get((int) target.getStat().calcStat(Stats.CRIT_DAMAGE_EVASION, 100, null, null)) < 100;
		}
		return success;
	}

	public static boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		return !(skill.isMagic() && skill.getSkillType() != L2SkillType.BLOW) && Rnd.getChance(target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill));
	}
}
