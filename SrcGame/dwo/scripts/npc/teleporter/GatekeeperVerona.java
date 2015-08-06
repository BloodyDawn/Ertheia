package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 18.01.13
 * Time: 21:13
 */
public class GatekeeperVerona extends Quest
{
	private static final int NPC = 30727;

	public GatekeeperVerona()
	{
		addAskId(NPC, -8);
		addAskId(NPC, -1001);
	}

	public static void main(String[] args)
	{
		new GatekeeperVerona();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -8:
				if(reply == 1)
				{
					npc.showTeleportList(player, 1);
					return null;
				}
				if(reply == 2)
				{
					npc.showTeleportList(player, 2);
					return null;
				}
				break;
			case -1001:
				if(reply == 1)
				{
					player.teleToLocation(90880, 12439, -4960);
					return null;
				}
		}
		return null;
	}

	//
}
