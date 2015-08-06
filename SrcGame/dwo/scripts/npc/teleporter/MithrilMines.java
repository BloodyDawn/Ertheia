package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;

public class MithrilMines extends Quest
{
	private static final Location[] data = {
		new Location(171946, -173352, 3440), new Location(175499, -181586, -904), new Location(173462, -174011, 3480),
		new Location(179299, -182831, -224), new Location(178591, -184615, 360), new Location(175499, -181586, -904)
	};

	private static final int npcId = 32652;

	public MithrilMines()
	{
		addAskId(npcId, -2512);
	}

	public static void main(String[] args)
	{
		new MithrilMines();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.isInsideRadius(173147, -173762, L2Npc.INTERACTION_DISTANCE, true))
		{
			if(reply == 1)
			{
				player.teleToLocation(data[0]);
			}
			else if(reply == 2)
			{
				player.teleToLocation(data[1]);
			}
		}
		else if(npc.isInsideRadius(181941, -174614, L2Npc.INTERACTION_DISTANCE, true))
		{
			if(reply == 1)
			{
				player.teleToLocation(data[2]);
			}
			else if(reply == 2)
			{
				player.teleToLocation(data[3]);
			}
		}
		else if(npc.isInsideRadius(179560, -182956, L2Npc.INTERACTION_DISTANCE, true))
		{
			if(reply == 1)
			{
				player.teleToLocation(data[4]);
			}
			else if(reply == 2)
			{
				player.teleToLocation(data[5]);
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.isInsideRadius(181941, -174614, L2Npc.INTERACTION_DISTANCE, true) ? "teleport_crystal_mine002.htm" : "teleport_crystal_mine001.htm";
	}
}