package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:25
 */
public class HandyBlockGuide extends Quest
{
	private static final int NPC = 32613;

	public HandyBlockGuide()
	{
		addAskId(NPC, -18161);
	}

	public static void main(String[] args)
	{
		new HandyBlockGuide();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -18161:
				if(reply == 1)
				{
					player.teleToLocation(-59157, -56906, -2032);
					return null;
				}
		}
		return null;
	}
}
