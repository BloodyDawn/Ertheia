package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.12.12
 * Time: 15:29
 */

public class StrongholdsTeleports extends Quest
{
	private static final int[] NPCs = {32163, 32181, 32184, 32186};

	public StrongholdsTeleports()
	{
		addAskId(NPCs, -31);
	}

	public static void main(String[] args)
	{
		new StrongholdsTeleports();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -31)
		{
			if(reply == 1)
			{
				if(player.getLevel() >= 20)
				{
					return npc.getServerName() + "005.htm";
				}
				else
				{
					npc.showTeleportList(player, 1);
					return null;
				}
			}
		}
		return null;
	}
}