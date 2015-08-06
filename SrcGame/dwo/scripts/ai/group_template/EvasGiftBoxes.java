package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class EvasGiftBoxes extends Quest
{
	private static final int GIFTBOX = 32342;

	private static final int KISSOFEVA = 1073;

	// index 0: without kiss of eva
	// index 1: with kiss of eva
	// chance,itemId,...
	private static final int[][] CHANCES = {{2, 9692, 1, 9693}, {100, 9692, 50, 9693}};

	public EvasGiftBoxes()
	{
		addKillId(GIFTBOX);
		addSpawnId(GIFTBOX);
	}

	public static void main(String[] args)
	{
		new EvasGiftBoxes();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == GIFTBOX)
		{
			int isKissOfEvaBuffed = 0;
			if(killer.getFirstEffect(KISSOFEVA) != null)
			{
				isKissOfEvaBuffed = 1;
			}
			for(int i = 0; i < CHANCES[isKissOfEvaBuffed].length; i += 2)
			{
				if(Rnd.getChance(CHANCES[isKissOfEvaBuffed][i]))
				{
					killer.addItem(ProcessType.NPC, CHANCES[isKissOfEvaBuffed][i + 1], 1, npc, true);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
}