package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:08
 */
public class GardenofGenesis extends Quest
{
	private static final int NPC = 33089;

	public GardenofGenesis()
	{
		addAskId(NPC, -500);
	}

	public static void main(String[] args)
	{
		new GardenofGenesis();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -500:
				if(reply == 1)
				{
					player.teleToLocation(207548, 112214, -2064);
					return null;
				}
		}
		return null;
	}
}
