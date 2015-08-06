package dwo.scripts.ai.individual;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class OutpostCaptain extends Quest
{
	private static final int CAPTAIN = 18466;
	private static final int[] DEFENDERS = {
		22357, 22358
	};
	private static final int DOORKEEPER = 32351;

	public OutpostCaptain()
	{
		addKillId(CAPTAIN);
		addSpawnId(CAPTAIN);
		addSpawnId(DOORKEEPER);
		addSpawnId(DEFENDERS);
	}

	public static void main(String[] args)
	{
		new OutpostCaptain();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("level_up"))
		{
			npc.getLocationController().delete();
			HellboundManager.getInstance().setLevel(9);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(HellboundManager.getInstance().getLevel() == 8)
		{
			addSpawn(DOORKEEPER, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0, false);
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);

		if(npc.getNpcId() == CAPTAIN)
		{
			L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(20250001);
			if(door != null)
			{
				door.closeMe();
			}
		}
		else if(npc.getNpcId() == DOORKEEPER)
		{
			startQuestTimer("level_up", 3000, npc, null);
		}

		return super.onSpawn(npc);
	}
}
