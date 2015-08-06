package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class SearchingMaster extends Quest
{
	private static final int[] mobs = {
		20965, 20966, 20967, 20968, 20969, 20970, 20971, 20972, 20973
	};

	public SearchingMaster()
	{
		addAttackId(mobs);
	}

	public static void main(String[] args)
	{
		new SearchingMaster();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(player == null)
		{
			return null;
		}

		npc.setIsRunning(true);
		((L2Attackable) npc).addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);

		return super.onAttack(npc, player, damage, isPet);
	}
}