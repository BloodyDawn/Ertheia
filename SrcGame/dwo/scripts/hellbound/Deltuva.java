package dwo.scripts.hellbound;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Deltuva extends Quest
{
	private static final int DELTUVA = 32313;

	public Deltuva()
	{
		addAskId(DELTUVA, -1006);
	}

	public static void main(String[] args)
	{
		new Deltuva();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				/*
				final QuestState hostQuest = player.getQuestState(_00132_MatrasCuriosity.class);
				if ((hostQuest == null) || !hostQuest.isCompleted())
				{
					player.teleToLocation(17934,283189,-9701);
					return null;
				}
				else
                */
				return "daltuva002.htm";
			}
		}
		return null;
	}
}