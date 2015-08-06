package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 26.01.13
 * Time: 19:12
 */
public class ZakenSsender extends Quest
{
	private static final int NPC = 32712;

	public ZakenSsender()
	{
		addAskId(NPC, -2124002);
	}

	public static void main(String[] args)
	{
		new ZakenSsender();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -2124002:
				if(reply == 1)
				{
					player.teleToLocation(52241, 218775, -3232);
					return null;
				}
		}
		return null;
	}
}
