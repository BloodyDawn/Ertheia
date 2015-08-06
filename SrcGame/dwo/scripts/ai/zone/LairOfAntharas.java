package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class LairOfAntharas extends Quest
{
	private static final int KNIGHT1 = 22844;
	private static final int KNIGHT2 = 22845;

	public LairOfAntharas()
	{
		addKillId(KNIGHT1);
		addKillId(KNIGHT2);
	}

	public static void main(String[] args)
	{
		new LairOfAntharas();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == KNIGHT1 && Rnd.getChance(30))
		{
			L2Npc mob = addSpawn(KNIGHT2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, true);
			mob.setRunning();
			((L2Attackable) mob).attackCharacter(killer);
		}
		else if(npc.getNpcId() == KNIGHT2 && Rnd.getChance(30))
		{
			L2Npc mob = addSpawn(22846, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, true);
			mob.setRunning();
			((L2Attackable) mob).addDamageHate(killer, 0, 999);
			((L2Attackable) mob).attackCharacter(killer);
		}
		return super.onKill(npc, killer, isPet);
	}
}