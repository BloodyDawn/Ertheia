package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.06.13
 * Time: 13:08
 */

public class Hansen extends Quest
{
	private static final int NPC = 33853;

	public Hansen()
	{
		addAskId(NPC, -1117);
	}

	public static void main(String[] args)
	{
		new Hansen();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			player.teleToLocation(109785, -41230, -2272);
		}
		return null;
	}
}