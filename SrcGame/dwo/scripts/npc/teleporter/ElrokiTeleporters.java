package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class ElrokiTeleporters extends Quest
{
	private static final int Oraochin = 32111;
	private static final int Gariachin = 32112;

	public ElrokiTeleporters()
	{
		addTeleportRequestId(Oraochin, Gariachin);
	}

	public static void main(String[] args)
	{
		new ElrokiTeleporters();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(player.isInCombat())
		{
			return npc.getServerName() + "002.htm";
		}
		if(npc.getNpcId() == Oraochin)
		{
			player.teleToLocation(4990, -1879, -3178);
		}
		else if(npc.getNpcId() == Gariachin)
		{
			player.teleToLocation(7557, -5513, -3221);
		}
		return null;
	}
}
