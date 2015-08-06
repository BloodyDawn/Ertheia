package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.RaidBossSpawnManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Darion extends Quest
{
	private static final int[] Doors = {20250004, 20250005, 20250006, 20250007, 20250009};

	public Darion(int id, String name, String descr)
	{
		super(id, name, descr);

		registerMobs(new int[]{25603}, QuestEventType.ON_SPAWN, QuestEventType.ON_KILL);

		RaidBossSpawnManager.StatusEnum status = RaidBossSpawnManager.getInstance().getRaidBossStatusId(25603);
		if(status == RaidBossSpawnManager.StatusEnum.DEAD)
		{
			for(int i : Doors)
			{
				DoorGeoEngine.getInstance().getDoor(i).openMe();
			}
		}
	}

	public static void main(String[] args)
	{
		new Darion(-1, "Darion", "ai");
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		for(int id : Doors)
		{
			DoorGeoEngine.getInstance().getDoor(id).openMe();
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		for(int id : Doors)
		{
			DoorGeoEngine.getInstance().getDoor(id).closeMe();
		}
		return super.onSpawn(npc);
	}
}
