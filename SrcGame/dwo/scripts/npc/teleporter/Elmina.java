package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 18:16
 */
public class Elmina extends Quest
{
	private static final int NPC = 32774;

	public Elmina()
	{
		addAskId(NPC, -415);
	}

	public static void main(String[] args)
	{
		new Elmina();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -415:
				if(reply == 1)
				{
					player.teleToLocation(-178262, 153430, 2472);
					return null;
				}
		}
		return null;
	}
}
