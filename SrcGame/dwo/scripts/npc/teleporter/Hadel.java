package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.11.12
 * Time: 23:13
 */

public class Hadel extends Quest
{
	private static final int NPC = 33344;

	public Hadel()
	{
		addAskId(NPC, 1);
	}

	public static void main(String[] args)
	{
		new Hadel();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 1)
		{
			if(reply == 1)
			{
				if(player.getLevel() >= 85 && player.isAwakened())
				{
					player.teleToLocation(-114700, 147909, -7720);
					return null;
				}
			}
		}
		return null;
	}
}