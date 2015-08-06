package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2-GodWorld Team
 * User: Yukio
 * Date: 10.09.12
 * Time: 21:16:07
 */

public class _00121_PavelTheGiants extends Quest
{
	// NPCs
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;

	public _00121_PavelTheGiants()
	{
		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR, YUMI);
	}

	public static void main(String[] args)
	{
		new _00121_PavelTheGiants();
	}

	@Override
	public int getQuestId()
	{
		return 121;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() >= 70 && !st.isCompleted())
		{
			st.startQuest();
			return "head_blacksmith_newyear_q0121_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == YUMI)
		{
			if(reply == 3 && cond == 1)
			{
				st.addExpAndSp(1959460, 2039940);
				st.exitQuest(QuestType.ONE_TIME);
				return "collecter_yumi_q0121_0201.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case NEWYEAR:
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 70)
					{
						return "head_blacksmith_newyear_q0121_0101.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "head_blacksmith_newyear_q0121_0103.htm";
					}
				}
				else
				{
					return "head_blacksmith_newyear_q0121_0105.htm";
				}
			case YUMI:
				if(cond == 1)
				{
					return "collecter_yumi_q0121_0101.htm";
				}
				break;
		}
		return null;
	}
}