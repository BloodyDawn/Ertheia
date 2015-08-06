package dwo.scripts.ai.individual;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.01.12
 * Time: 2:59
 */

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

public class AncientEgg extends Quest
{
	public AncientEgg()
	{
		addAttackId(18344);
	}

	public static void main(String[] args)
	{
		new AncientEgg();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		npc.getKnownList().getKnownCharactersInRadius(1000).stream().filter(mobs -> mobs instanceof L2MonsterInstance && player != null).forEach(mobs -> {
			mobs.setTarget(player);
			mobs.setRunning();
			((L2Attackable) mobs).addDamageHate(player, 0, 999);
			mobs.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		});
		return super.onAttack(npc, player, damage, isPet, skill);
	}
}