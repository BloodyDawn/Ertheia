package dwo.scripts.ai.specific;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SpawnOnKill extends Quest
{
	private static final TIntObjectHashMap<int[]> SPAWNS = new TIntObjectHashMap<>();

	static
	{
		SPAWNS.put(22704, new int[]{22706}); //Turka Follower's Ghost
		SPAWNS.put(22705, new int[]{22707}); //Turka Commander's Ghost
	}

	public SpawnOnKill()
	{
		addKillId(22704, 22705);
	}

	public static void main(String[] args)
	{
		new SpawnOnKill();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(SPAWNS.containsKey(npcId))
		{
			if(Rnd.getChance(5)) //mob that spawn only on certain chance
			{
				for(int val : SPAWNS.get(npcId))
				{
					addSpawn(val, npc);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
}