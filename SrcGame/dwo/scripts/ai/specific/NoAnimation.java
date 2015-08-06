package dwo.scripts.ai.specific;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;

import java.util.Set;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.08.11
 * Time: 13:44
 */

public class NoAnimation extends Quest
{
	public NoAnimation()
	{
		int[] npcIds = {
			// Sel Mahum Train mobs
			22775, 22776, 22777, 22778, 22779, 22780, 22781, 22782, 22783, 22784, 22785,
			// Sel Mahum Leader Mobs
			22786, 22787, 22788,
			// Sel Mahum Chef
			18908,
			// Devastated Castle mobs
			35411, 35412, 35413, 35414, 35415, 35416,
			// Fortress of Resistance mobs
			35632, 35633, 35634, 35635, 35636, 35637,
			// Talking Island corpse
			32962, 32963, 32964
		};
		registerMobs(npcIds);
		for(int npcId : npcIds)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				spawn.getLastSpawn().setIsNoAnimation(true);
			}
		}
		addSpawnId(npcIds);
	}

	public static void main(String[] args)
	{
		new NoAnimation();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoAnimation(true);
		return super.onSpawn(npc);
	}
}
