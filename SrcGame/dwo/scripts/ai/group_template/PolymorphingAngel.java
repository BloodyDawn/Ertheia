package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Angel spawns...when one of the angels in the keys dies, the other angel will spawn.
 */
public class PolymorphingAngel extends Quest
{
	private static final TIntIntHashMap ANGELSPAWNS = new TIntIntHashMap();

	static
	{
		ANGELSPAWNS.put(20830, 20859);
		ANGELSPAWNS.put(21067, 21068);
		ANGELSPAWNS.put(21062, 21063);
		ANGELSPAWNS.put(20831, 20860);
		ANGELSPAWNS.put(21070, 21071);
	}

	public PolymorphingAngel()
	{
		addKillId(20830, 21067, 21062, 20831, 21070);
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new PolymorphingAngel();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(ANGELSPAWNS.containsKey(npcId))
		{
			L2Attackable newNpc = (L2Attackable) addSpawn(ANGELSPAWNS.get(npcId), npc);
			newNpc.setRunning();
		}
		return super.onKill(npc, killer, isPet);
	}
}