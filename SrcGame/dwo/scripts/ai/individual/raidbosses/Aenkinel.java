package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Aenkinel extends Quest
{
	private static final int AENKINEL1 = 25690;
	private static final int AENKINEL2 = 25691;
	private static final int AENKINEL3 = 25692;
	private static final int AENKINEL4 = 25693;
	private static final int AENKINEL5 = 25694;
	private static final int AENKINEL6 = 25695;

	public Aenkinel()
	{
		addKillId(AENKINEL1, AENKINEL2, AENKINEL3, AENKINEL4, AENKINEL5, AENKINEL6);
	}

	public static void main(String[] args)
	{
		new Aenkinel();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == AENKINEL1 || npcId == AENKINEL2 || npcId == AENKINEL3 || npcId == AENKINEL4 || npcId == AENKINEL5 || npcId == AENKINEL6)
		{
			int instanceId = npc.getInstanceId();
			addSpawn(18820, -121524, -155073, -6752, 64792, false, 0, false, instanceId);
			addSpawn(18819, -121486, -155070, -6752, 57739, false, 0, false, instanceId);
			addSpawn(18819, -121457, -155071, -6752, 49471, false, 0, false, instanceId);
			addSpawn(18819, -121428, -155070, -6752, 41113, false, 0, false, instanceId);
		}
		return super.onKill(npc, player, isPet);
	}
}