package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Hallate extends Quest
{
	// Hallate NpcID
	private static final int HALLATE = 25220;

	// Hallate Z coords
	private static final int z1 = -2150;
	private static final int z2 = -1650;

	public Hallate()
	{
		int[] mobs = {HALLATE};
		registerMobs(mobs);
	}

	public static void main(String[] args)
	{
		new Hallate();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == HALLATE)
		{
			int z = npc.getZ();
			if(z > z2 || z < z1)
			{
				npc.teleToLocation(113548, 17061, -2125);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}