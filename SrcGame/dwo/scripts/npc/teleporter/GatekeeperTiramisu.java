package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.12.12
 * Time: 22:53
 */

public class GatekeeperTiramisu extends Quest
{
	private static final int NPC = 30429;

	public GatekeeperTiramisu()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new GatekeeperTiramisu();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(Rnd.getChance(50))
		{
			player.teleToLocation(-112899, 234942, -3693);
		}
		else
		{
			player.teleToLocation(-112817, 235183, -3690);
		}
		return null;
	}
}