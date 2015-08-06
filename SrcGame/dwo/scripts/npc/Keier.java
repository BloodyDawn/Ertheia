package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._00115_TheOtherSideOfTruth;
import dwo.scripts.quests._10283_RequestOfIceMerchant;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.11.12
 * Time: 8:45
 */

public class Keier extends Quest
{
	private static final int KeierNPC = 32022;

	public Keier()
	{
		addFirstTalkId(KeierNPC);
	}

	public static void main(String[] args)
	{
		new Keier();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(_00115_TheOtherSideOfTruth.class);
		if(st != null && st.isStarted())
		{
			return "keier002.htm";
		}
		st = player.getQuestState(_10283_RequestOfIceMerchant.class);
		if(st != null)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "keier003.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "keier004.htm";
			}
		}
		return "keier001.htm";
	}
}
