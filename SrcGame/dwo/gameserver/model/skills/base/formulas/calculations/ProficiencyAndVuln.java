package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TraitType;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:40
 */

public class ProficiencyAndVuln
{
	public static double calcSkillVulnerability(L2Character attacker, L2Character target, L2Skill skill)
	{
		double multiplier = 0; // initialize...

		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if(skill != null)
		{
			// first, get the natural template vulnerability values for the target
			Stats stat = skill.getStat();
			if(stat != null)
			{
				switch(stat)
				{
					case AGGRESSION:
						multiplier = target.getTemplate().getBaseAggressionVuln();
						break;
					case BLEED:
						multiplier = target.getTemplate().getBaseBleedVuln();
						break;
					case POISON:
						multiplier = target.getTemplate().getBasePoisonVuln();
						break;
					case STUN:
						multiplier = target.getTemplate().getBaseStunVuln();
						break;
					case ROOT:
						multiplier = target.getTemplate().getBaseRootVuln();
						break;
					case MOVEMENT:
						multiplier = target.getTemplate().getBaseMovementVuln();
						break;
					case SLEEP:
						multiplier = target.getTemplate().getBaseSleepVuln();
						break;
				}
			}

			// Finally, calculate skilltype vulnerabilities
			multiplier = calcSkillTraitVulnerability(multiplier, target, skill);
		}
		return multiplier;
	}

	public static double calcSkillProficiency(L2Skill skill, L2Character attacker, L2Character target)
	{
		double multiplier = 0;

		if(skill != null)
		{
			// Calculate trait-type vulnerabilities
			multiplier = calcSkillTraitProficiency(multiplier, attacker, target, skill);
		}

		return multiplier;
	}

	public static double calcSkillTraitVulnerability(double multiplier, L2Character target, L2Skill skill)
	{
		if(skill == null)
		{
			return multiplier;
		}

		L2TraitType trait = skill.getTraitType();
		// First check if skill have trait set
		// If yes, use correct vuln
		if(trait != null && trait != L2TraitType.NONE)
		{
			switch(trait)
			{
				case BLEED:
					multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
					break;
				case BOSS:
					multiplier = target.calcStat(Stats.BOSS_VULN, multiplier, target, null);
					break;
				//case DEATH:
				case DERANGEMENT:
					multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
					break;
				//case ETC:
				case GUST:
					multiplier = target.calcStat(Stats.GUST_VULN, multiplier, target, null);
					break;
				case HOLD:
					multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
					break;
				case PHYSICAL_BLOCKADE:
					multiplier = target.calcStat(Stats.PHYSICAL_BLOCKADE_VULN, multiplier, target, null);
					break;
				case POISON:
					multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
					break;
				case SHOCK:
					multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
					break;
				case VALAKAS:
					multiplier = target.calcStat(Stats.VALAKAS_VULN, multiplier, target, null);
					break;
				case KNOCKBACK:
					multiplier = target.calcStat(Stats.KNOCKBACK_VULN, multiplier, target, null);
					break;
				case KNOCKDOWN:
					multiplier = target.calcStat(Stats.KNOCKDOWN_VULN, multiplier, target, null);
					break;
				case FLYUP:
					multiplier = target.calcStat(Stats.FLYUP_VULN, multiplier, target, null);
					break;
				case ATTRACT:
					multiplier = target.calcStat(Stats.ATTRACT_VULN, multiplier, target, null);
					break;
			}
		}
		else
		{
			// Since not all traits are handled by trait parameter
			// rest is checked by skillType or isDebuff Boolean.
			L2SkillType type = skill.getSkillType();
			if(type == L2SkillType.BUFF)
			{
				multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
			}
			else if(type == L2SkillType.STEAL_BUFF)
			{
				multiplier = target.calcStat(Stats.CANCEL_VULN, multiplier, target, null);
			}
		}
		return multiplier;
	}

	public static double calcSkillTraitProficiency(double multiplier, L2Character attacker, L2Character target, L2Skill skill)
	{
		if(skill == null)
		{
			return multiplier;
		}

		L2TraitType trait = skill.getTraitType();
		// First check if skill have trait set
		// If yes, use correct vuln
		if(trait != null && trait != L2TraitType.NONE)
		{
			switch(trait)
			{
				case BLEED:
					multiplier = attacker.calcStat(Stats.BLEED_PROF, multiplier, target, null);
					break;
				//case BOSS:
				//case DEATH:
				case DERANGEMENT:
					multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
					break;
				//case ETC:
				//case GUST:
				case HOLD:
					multiplier = attacker.calcStat(Stats.ROOT_PROF, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = attacker.calcStat(Stats.PARALYZE_PROF, multiplier, target, null);
					break;
				//case PHYSICAL_BLOCKADE:
				case POISON:
					multiplier = attacker.calcStat(Stats.POISON_PROF, multiplier, target, null);
					break;
				case SHOCK:
					multiplier = attacker.calcStat(Stats.STUN_PROF, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = attacker.calcStat(Stats.SLEEP_PROF, multiplier, target, null);
					break;
				case VALAKAS:
					multiplier = attacker.calcStat(Stats.VALAKAS_PROF, multiplier, target, null);
					break;
				case FLYUP:
					multiplier = attacker.calcStat(Stats.FLYUP_PROF, multiplier, target, null);
					break;
				case KNOCKBACK:
					multiplier = attacker.calcStat(Stats.KNOCKBACK_PROF, multiplier, target, null);
					break;
				case KNOCKDOWN:
					multiplier = attacker.calcStat(Stats.KNOCKDOWN_PROF, multiplier, target, null);
					break;
				case ATTRACT:
					multiplier = attacker.calcStat(Stats.ATTRACT_PROF, multiplier, target, null);
					break;
			}
		}
		else
		{
			// Since not all traits are handled by skill parameter
			// rest is checked by skillType or isDebuff Boolean.
			L2SkillType type = skill.getSkillType();
			if(type == L2SkillType.DEBUFF || skill.isDebuff())
			{
				multiplier = target.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
			}
			else if(type == L2SkillType.CANCEL || type == L2SkillType.STEAL_BUFF)
			{
				multiplier = attacker.calcStat(Stats.CANCEL_PROF, multiplier, target, null);
			}
		}
		return multiplier;
	}

	public static double calcResMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		double vuln = calcSkillVulnerability(attacker, target, skill);
		double prof = calcSkillProficiency(skill, attacker, target);
		double resMod = 1 + (vuln + prof) / 100;
		return Math.min(Math.max(resMod, 0.1), 1.9);
	}
}