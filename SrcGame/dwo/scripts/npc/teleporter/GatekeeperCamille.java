package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 04.02.13
 * Time: 21:27
 */
public class GatekeeperCamille extends Quest
{
	private static final int NPC = 33836;

	public GatekeeperCamille()
	{
		addFirstTalkId(NPC);
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new GatekeeperCamille();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		npc.showTeleportList(player, 1);
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "gatekeeper_camille.htm";
	}
}
