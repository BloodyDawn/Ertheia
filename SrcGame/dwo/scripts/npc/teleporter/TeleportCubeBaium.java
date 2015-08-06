package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:51
 */
public class TeleportCubeBaium extends Quest
{
	private static final int[] NPCs = {31842, 31843};

	public TeleportCubeBaium()
	{
		addTeleportRequestId(NPCs);
	}

	public static void main(String[] args)
	{
		new TeleportCubeBaium();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		int i0 = Rnd.get(3);
		int i1;
		int i2;
		int i3;
		if(i0 == 0)
		{
			i1 = 108784 + Rnd.get(100);
			i2 = 16000 + Rnd.get(100);
			i3 = -4928;
		}
		else if(i0 == 1)
		{
			i1 = 113824 + Rnd.get(100);
			i2 = 10448 + Rnd.get(100);
			i3 = -5164;
		}
		else
		{
			i1 = 115488 + Rnd.get(100);
			i2 = 22096 + Rnd.get(100);
			i3 = -5168;
		}
		player.teleToLocation(i1, i2, i3);
		return null;
	}
}
