package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Barakiel extends Quest
{
	// Barakiel NpcID
	private static final int BARAKIEL = 25325;
	// Barakiel Z coords
	private static final int x1 = 89800;
	private static final int x2 = 93200;
	private static final int y1 = -87038;

	public Barakiel()
	{
		addAttackId(BARAKIEL);
	}

	public static void main(String[] args)
	{
		new Barakiel();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == BARAKIEL)
		{
			int x = npc.getX();
			int y = npc.getY();
			if(x < x1 || x > x2 || y < y1)
			{
				npc.teleToLocation(91008, -85904, -2736);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}