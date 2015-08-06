package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

public class PowderKeg extends Quest
{
	public PowderKeg()
	{
		addAttackId(18622);
	}

	public static void main(String[] args)
	{
		new PowderKeg();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skil)
	{
		if(!npc.isDead())
		{
			npc.doCast(new SkillHolder(5714, 1).getSkill());
		}
		return super.onAttack(npc, player, damage, isPet, skil);
	}
}
