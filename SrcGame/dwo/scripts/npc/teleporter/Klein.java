package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 17:33
 */

public class Klein extends Quest
{
	private static final int NPC = 31540;

	public Klein()
	{
		addAskId(NPC, -6);
	}

	public static void main(String[] args)
	{
		new Klein();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -6)
		{
			if(player.getItemsCount(7267) > 0)
			{
				player.teleToLocation(183813, -115157, -3303);
				return null;
			}
			else
			{
				return "watcher_valakas_klein008.htm";
			}
		}
		return null;
	}
}