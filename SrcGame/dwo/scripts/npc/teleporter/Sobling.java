package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:38
 */
public class Sobling extends Quest
{
	private static final int NPC = 31147;

	public Sobling()
	{
		addAskId(NPC, -2124001);
	}

	public static void main(String[] args)
	{
		new Sobling();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -2124001:
				switch(reply)
				{
					case 1:
						player.teleToLocation(183985, 61424, -3992);
						return null;
					case 2:
						player.teleToLocation(191754, 56760, -7624);
						return null;
				}
		}
		return null;
	}
}
