package dwo.scripts.npc.castle;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.10.12
 * Time: 1:47
 */

public class CastleCropManufacture extends Quest
{
	private static final int[] NPCs = {35098, 35140, 35182, 35224, 35272, 35314, 35361, 35507, 35553};

	public CastleCropManufacture()
	{
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new CastleCropManufacture();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getClan() != null)
		{
			if(npc.getCastle().getOwnerId() != player.getClanId())
			{
				return "gludio_smith002.htm";
			}
		}
		else if(npc.getCastle().getZone().isSiegeActive())
		{
			return "gludio_smith002.htm";
		}
		return npc.getServerName() + "001.htm";
	}
}