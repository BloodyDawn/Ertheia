package dwo.gameserver.model.skills.base.formulas.calculations;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 22:38
 */

public class ManaDamage
{
	public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
	{
		// AI SpiritShot
		if(attacker instanceof L2Npc)
		{
			ss = ((L2Npc) attacker)._spiritshotcharged;
			((L2Npc) attacker)._spiritshotcharged = false;
		}
		// Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable;
		double mp = target.getMaxMp();

		if(bss)
		{
			mAtk *= 4;
		}
		else if(ss)
		{
			mAtk *= 2;
		}

		double damage = Math.sqrt(mAtk) * skill.getPower(attacker, target, isPvP, isPvE) * mp / 97 / mDef;
		damage *= 1 + ProficiencyAndVuln.calcSkillVulnerability(attacker, target, skill) / 100;
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
}
