package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 17:40
 */

public class TeleportCubeAntaras extends Quest
{
	private static final int NPC = 31859;

	public TeleportCubeAntaras()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new TeleportCubeAntaras();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		int xRand = 79800 + Rnd.get(600);
		int yRand = 151200 + Rnd.get(1100);
		player.teleToLocation(xRand, yRand, -3534);
		return null;
	}
}