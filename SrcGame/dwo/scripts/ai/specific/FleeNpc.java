package dwo.scripts.ai.specific;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;

public class FleeNpc extends Quest
{
	private int[] _npcId = {20432, 22228, 18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157};

	public FleeNpc()
	{
		addAttackId(_npcId);
	}

	public static void main(String[] args)
	{
		new FleeNpc();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() >= 18150 && npc.getNpcId() <= 18157)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + Rnd.get(-40, 40), npc.getY() + Rnd.get(-40, 40), npc.getZ(), npc.getHeading()));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		if(npc.getNpcId() == 20432 || npc.getNpcId() == 22228)
		{
			if(Rnd.get(3) == 2)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + Rnd.get(-200, 200), npc.getY() + Rnd.get(-200, 200), npc.getZ(), npc.getHeading()));
			}
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}
