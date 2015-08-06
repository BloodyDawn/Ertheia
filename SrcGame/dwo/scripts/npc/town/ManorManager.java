package dwo.scripts.npc.town;

import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 15.01.13
 * Time: 14:39
 */

public class ManorManager extends Quest
{
	private static final int[] NPCs = {
		35103, 35145, 35187, 35229, 35230, 35231, 35277, 35319, 35366, 35512, 35558, 35644, 35645, 36456
	};

	public ManorManager()
	{
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new ManorManager();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(CastleManorManager.getInstance().isDisabled())
		{
			return "npcdefault.htm";
		}

		return npc.getCastle() != null && npc.getCastle().getCastleId() > 0 && player.isClanLeader() && npc.getCastle().getOwnerId() == player.getClanId() ? "castle_merchant002.htm" : "castle_merchant001.htm";
	}
}