package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class ReedfieldHerbs extends Quest
{
	private static final int[] mobs = {
		22650, 22651, 22652, 22653, 22654, 22655, 22656, 22657, 22658, 22659
	};

	public ReedfieldHerbs()
	{
		addKillId(mobs);
	}

	public static void main(String[] args)
	{
		new ReedfieldHerbs();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int r = Rnd.get(100);
		if(r <= 55)
		{
			((L2MonsterInstance) npc).dropItem(killer, 8603, 1);
		}
		else if(r < 85)
		{
			((L2MonsterInstance) npc).dropItem(killer, 8604, 1);
		}
		else if(r < 90)
		{
			((L2MonsterInstance) npc).dropItem(killer, 8605, 1);
		}
		return super.onKill(npc, killer, isPet);
	}
}