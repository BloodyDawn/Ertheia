package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.12.12
 * Time: 22:42
 */

public class AncientTeleporter extends Quest
{
	private static final int NPC = 32714;

	public AncientTeleporter()
	{
		addAskId(NPC, -505);
	}

	public static void main(String[] args)
	{
		new AncientTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -505)
		{
			if(reply == 2)
			{
				if(player.getAdenaCount() >= 50000)
				{
					player.reduceAdena(ProcessType.NPC, 50000, npc, true);
					player.teleToLocation(43835, -47749, -792);
				}
				else
				{
					return "ancient_teleporter003.htm";
				}
			}
		}
		return null;
	}
}