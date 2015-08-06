package dwo.scripts.ai.specific;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class SummonOnAttack extends Quest
{
	private static final int[] mobs = {
		20965, 20966, 20967, 20968, 20969, 20970, 20971, 20972, 20973
	};

	public SummonOnAttack()
	{
		addAttackId(mobs);
	}

	public static void main(String[] args)
	{
		new SummonOnAttack();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		L2Character attacker = player;

		npc.setIsRunning(true);
		((L2Attackable) npc).addDamageHate(attacker, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

		return super.onAttack(npc, player, damage, isPet);
	}
}
