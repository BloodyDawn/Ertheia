package dwo.scripts.ai.individual;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;

import java.util.Set;

public class Sprignant extends Quest
{
	private static final int SPRIGNANT_ANESTHESIA = 18345;
	private static final int SPRIGNANT_POISON = 18346;

	public Sprignant()
	{
		int[] mobs = {
			SPRIGNANT_ANESTHESIA, SPRIGNANT_POISON
		};
		addSpawnId(mobs);
		for(int npcId : mobs)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				startQuestTimer("Skill", 5000, spawn.getLastSpawn(), null);
			}
		}
	}

	public static void main(String[] args)
	{
		new Sprignant();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		L2Attackable mob = null;

		if(npc instanceof L2Attackable)
		{
			mob = (L2Attackable) npc;
		}

		if(mob == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("Skill"))
		{
			int npcId = npc.getNpcId();
			if(npcId == SPRIGNANT_ANESTHESIA)
			{
				npc.setTarget(npc);
				npc.doCast(new SkillHolder(5085, 1).getSkill());
			}
			else if(npcId == SPRIGNANT_POISON)
			{
				npc.setTarget(npc);
				npc.doCast(new SkillHolder(5086, 1).getSkill());
			}
			startQuestTimer("Skill", 15000, npc, null);
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("Skill", 5000, npc, null);
		return super.onSpawn(npc);
	}
}