package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Darnel extends Quest
{
	public Darnel()
	{
		addKillId(25531);
	}

	public static void main(String[] args)
	{
		new Darnel();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		addSpawn(32279, 152761, 145950, -12588, 0, false, 0, false, player.getInstanceId());
		return super.onKill(npc, player, isPet);
	}
}
