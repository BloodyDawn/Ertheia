package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class DragonKnights extends Quest
{
	private static final int[] _mobsToKill = {
		22839, 22840, 22841, 22842, 22843
	};
	private static long _lastSpawn = System.currentTimeMillis();

	public DragonKnights()
	{
		addKillId(_mobsToKill);
	}

	public static void main(String[] args)
	{
		new DragonKnights();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(System.currentTimeMillis() - _lastSpawn < 60 * 1000)
		{
			return super.onKill(npc, killer, isPet);
		}
		_lastSpawn = System.currentTimeMillis();

		L2Npc mob = null;

		if(Rnd.getChance(1))
		{
			mob = addSpawn(22844, npc, true);
		}

		// TODO: This probably should be isAttackable(killer)
		if(mob != null && mob.isAttackable())
		{
			((L2Attackable) mob).addDamageHate(killer, 0, 9999);
		}

		return super.onKill(npc, killer, isPet);
	}
}
