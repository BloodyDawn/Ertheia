package dwo.scripts.ai.specific;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;

import java.util.Set;

/**
 * TODO: Перенести в АИ
 */
public class SeeThroughSilentMove extends Quest
{
	private static final int[] MOBIDS = {
		18001, 18002, 18329, 18330, 18331, 18333, 18334, 18335, 18336, 18337, 18338, 22199, 22215, 22216, 22217, 22327,
		22360, 22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22546, 22547, 22550, 22551, 22552, 22581,
		22593, 22596, 22597, 22746, 22747, 22748, 22749, 22750, 22751, 22752, 22753, 22754, 22755, 22756, 22757, 22758,
		22759, 22760, 22761, 22762, 22763, 22764, 22765, 22794, 22795, 22796, 22797, 22798, 22799, 22800, 22820, 22821,
		22831, 22832, 22833, 22834, 22859, 25544, 27347, 27348, 27349, 27350, 29009, 29010, 29011, 29012, 29013, 29162,
		29163, 29173, 32350, 32592,
		// Логово Антараса
		22857, 22850, 22843,
		// Королева муравьев
		29001,
		// Алтарь Шилен
		23132
	};

	public SeeThroughSilentMove()
	{
		for(int npcId : MOBIDS)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			spawns.stream().filter(spawn -> spawn.getLastSpawn() != null && spawn.getLastSpawn().isAttackable()).forEach(spawn -> ((L2Attackable) spawn.getLastSpawn()).setSeeThroughSilentMove(true));
		}
		addSpawnId(MOBIDS);
	}

	public static void main(String[] args)
	{
		new SeeThroughSilentMove();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc instanceof L2Attackable)
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		return super.onSpawn(npc);
	}
}