package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class GraciaHeart extends Quest
{
	private static final int EmergyCompressor = 36570;

	public GraciaHeart()
	{
		addTeleportRequestId(EmergyCompressor);
	}

	public static void main(String[] args)
	{
		new GraciaHeart();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(player.getLevel() >= 75 && player.isFlying() && player.isTransformed())
		{
			player.teleToLocation(-204288, 242026, 1744);
			return null;
		}
		else
		{
			return npc.getServerName() + "002.htm";
		}
	}
}