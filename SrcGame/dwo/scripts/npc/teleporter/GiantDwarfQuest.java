package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 26.01.13
 * Time: 19:20
 */

public class GiantDwarfQuest extends Quest
{
	private static final int NPC = 32711;

	public GiantDwarfQuest()
	{
		addAskId(NPC, -2519003);
	}

	public static void main(String[] args)
	{
		new GiantDwarfQuest();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -2519003:
				if(reply == 1)
				{
					player.teleToLocation(183985, 61424, -3992);
					return null;
				}
		}
		return null;
	}
}
