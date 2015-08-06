package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 18:01
 */

public class TeleportCubeValakas extends Quest
{
	private static final int NPC = 31759;

	public TeleportCubeValakas()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new TeleportCubeValakas();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		int xRand = 150037 + Rnd.get(500);
		int yRand = -57720 + Rnd.get(500);
		player.teleToLocation(xRand, yRand, -2976);
		return null;
	}
}