package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class FairyTrees extends Quest
{
	private static final int[] mobs = {27185, 27186, 27187, 27188};

	public FairyTrees()
	{
		registerMobs(mobs, QuestEventType.ON_KILL);
		addSpawnId(27189);
	}

	public static void main(String[] args)
	{
		new FairyTrees();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(ArrayUtils.contains(mobs, npcId))
		{
			for(int i = 0; i < 20; i++)
			{
				L2Attackable newNpc = (L2Attackable) addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000);
				L2Character originalKiller = isPet ? killer.getPets().getFirst() : killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalKiller, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
				if(Rnd.get(1, 2) == 1)
				{
					SkillHolder skill = new SkillHolder(4243, 1);
					if(skill.getSkill() != null && originalKiller != null)
					{
						skill.getSkill().getEffects(newNpc, originalKiller);
					}
				}
			}
		}

		return super.onKill(npc, killer, isPet);
	}
}