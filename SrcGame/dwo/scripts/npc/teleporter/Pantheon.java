package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:15
 */
public class Pantheon extends Quest
{
	private static final int NPC = 32972;

	public Pantheon()
	{
		addAskId(NPC, -3525);
	}

	public static void main(String[] args)
	{
		new Pantheon();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -3525:
				if(reply == 1)
				{
					player.teleToLocation(-114711, 243911, -7968);
					return null;
				}
		}
		return null;
	}
}
