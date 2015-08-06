package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:35
 */

public class GiantDwarf extends Quest
{
	private static final int NPC = 32649;

	public GiantDwarf()
	{
		addAskId(NPC, -2519002);
	}

	public static void main(String[] args)
	{
		new GiantDwarf();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -2519002:
				if(reply == 1)
				{
					player.teleToLocation(191754, 56760, -7624);
					return null;
				}
		}
		return null;
	}
}
