package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

public class ForgeOfGods extends Quest
{
	private static final int[] _mobsToKill = {
		22634, 22635, 21378, 22637, 22639, 21654, 22638, 22641, 22644, 22642, 22643, 22646, 21388, 22647, 21393, 21657,
		22648, 22649
	};

	private static final int TAR = 18804;

	public ForgeOfGods()
	{
		addKillId(_mobsToKill);
		addAggroRangeEnterId(TAR);
		addSpawnId(TAR);
		onSpawnRerun(TAR);
	}

	public static void main(String[] args)
	{
		new ForgeOfGods();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("Skill"))
		{
			if(Util.checkIfInRange(250, npc, player, true))
			{
				int level = 0;
				L2Effect[] effects = player.getAllEffects();
				if(effects != null && effects.length > 0)
				{
					for(L2Effect e : effects)
					{
						if(e.getSkill().getId() == 6142)
						{
							level = e.getSkill().getLevel();
							if(level < 3)
							{
								e.exit();
							}
						}
					}
				}
				if(level < 3)
				{
					++level;
					L2Skill tempSkill = SkillTable.getInstance().getInfo(6142, level);
					tempSkill.getEffects(npc, player);
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int rand = Rnd.get(100);
		L2Npc mob = null;

		if(rand < 5)
		{
			mob = addSpawn(18002, npc, true);
		}
		else if(rand < 10)
		{
			mob = addSpawn(18801, npc, true);
		}
		else if(rand < 15)
		{
			mob = addSpawn(18800, npc, true);
		}
		else if(rand < 20)
		{
			mob = addSpawn(18799, npc, true);
		}

		if(mob != null && mob.isL2Attackable())
		{
			((L2Attackable) mob).addDamageHate(killer, 0, 9999);
			mob.setIsOverloaded(true);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == TAR && npc instanceof L2Attackable)
		{
			npc.setIsInvul(true);
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
			npc.setIsOverloaded(true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == TAR)
		{
			startQuestTimer("Skill", 2500, npc, player);
		}
		return null;
	}
}
