package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.06.13
 * Time: 13:38
 */

public class Rugoness extends Quest
{
	private static final int NPC = 33852;

	public Rugoness()
	{
		addAskId(NPC, -1116);
	}

	public static void main(String[] args)
	{
		new Rugoness();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			player.teleToLocation(149383, -82979, -5560);
		}
		return null;
	}
}