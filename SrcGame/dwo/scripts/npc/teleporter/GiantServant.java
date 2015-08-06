package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.10.11
 * Time: 22:04
 */

public class GiantServant extends Quest
{
	private static final int[] GiantServants = {33560, 33561, 33562, 33563, 33564, 33565, 33566, 33567, 33568, 33569};

	public GiantServant()
	{
		addTeleportRequestId(GiantServants);
	}

	public static void main(String[] args)
	{
		new GiantServant();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(player.isTransformed() || player.isFlyingMounted() || player.isFlying())
		{
			return "dimension_keeper1003.htm";
		}
		if(player.isAwakened())
		{
			if(player.getLevel() < 90 && player.getLevel() >= 85)
			{
				npc.showTeleportList(player, 1);
			}
			else if(player.getLevel() >= 90)
			{
				npc.showTeleportList(player, 2);
			}
			return null;
		}
		else
		{
			return "dimension_keeper1002.htm";
		}
	}
}
