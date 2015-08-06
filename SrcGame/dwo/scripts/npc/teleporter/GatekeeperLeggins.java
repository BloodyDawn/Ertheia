package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:56
 */
public class GatekeeperLeggins extends Quest
{
	private static final int NPC = 32189;

	public GatekeeperLeggins()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new GatekeeperLeggins();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		npc.showTeleportList(player, 1);
		return null;
	}
}
