package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Kernon extends Quest
{
	// Kernon NpcID
	private static final int KERNON = 25054;

	// Kernon Z coords
	private static final int z1 = 3900;
	private static final int z2 = 4300;

	public Kernon()
	{
		int[] mobs = {KERNON};
		registerMobs(mobs);
	}

	public static void main(String[] args)
	{
		new Kernon();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == KERNON)
		{
			int z = npc.getZ();
			if(z > z2 || z < z1)
			{
				npc.teleToLocation(113420, 16424, 3969);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}