package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 18:23
 */

public class TeleportCubeSailren extends Quest
{
	private static final int NPC = 32107;

	public TeleportCubeSailren()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new TeleportCubeSailren();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(Rnd.getChance(40))
		{
			player.teleToLocation(10610, -24035, -3676);
		}
		else if(Rnd.getChance(70))
		{
			player.teleToLocation(10703, -24041, -3673);
		}
		else
		{
			player.teleToLocation(10769, -24107, -3672);
		}
		return null;
	}
}