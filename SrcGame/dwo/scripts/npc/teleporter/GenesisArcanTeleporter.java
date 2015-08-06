package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.12.12
 * Time: 23:16
 */

public class GenesisArcanTeleporter extends Quest
{
	private static final int NPC = 33090;

	public GenesisArcanTeleporter()
	{
		addAskId(NPC, -501);
	}

	public static void main(String[] args)
	{
		new GenesisArcanTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -501)
		{
			player.teleToLocation(210831, 89441, -1144);
			return null;
		}
		return null;
	}
}