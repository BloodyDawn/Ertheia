package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.12.12
 * Time: 22:02
 */

public class GludioTeleporter extends Quest
{
	private static final int[] NPCs = {33085, 33086, 33087, 33088};

	public GludioTeleporter()
	{
		addAskId(NPCs, -507);
		addAskId(NPCs, -508);
		addAskId(NPCs, -509);
		addAskId(NPCs, -510);
	}

	public static void main(String[] args)
	{
		new GludioTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -507:
			case -508:
			case -509:
			case -510:
				if(reply == 1)
				{
					player.teleToLocation(-14567, 123872, -3112);
					return null;
				}
		}
		return null;
	}
}