package dwo.scripts.ai.individual;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

import java.util.Set;

public class DrakeWalkers extends Quest
{
	private static final int[] DRAKES = {
		22848, 22849, 22850, 22851,
	}; // Must be sorted!

	private static final int[] ROUTE_ID = {
		19, 20, 21
	};

	public DrakeWalkers()
	{
		addSpawnId(DRAKES);

		for(int npcId : DRAKES)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				onSpawn(spawn.getLastSpawn());
			}
		}
	}

	public static void main(String[] args)
	{
		new DrakeWalkers();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		WalkingManager.getInstance().startMoving(npc, ROUTE_ID[Rnd.get(ROUTE_ID.length)]);
		return super.onSpawn(npc);
	}
}
