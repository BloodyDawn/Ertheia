package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * LGOD Team
 * User: Yukio
 * Date: 10.09.12
 * Time: 21:16:07
 */

public class _00122_OminousNews extends Quest
{
	// NPCs
	private static final int MOIRA = 31979;
	private static final int KARUDA = 32017;

	public _00122_OminousNews()
	{
		addStartNpc(MOIRA);
		addTalkId(MOIRA, KARUDA);
	}

	public static void main(String[] args)
	{
		new _00122_OminousNews();
	}

	@Override
	public int getQuestId()
	{
		return 122;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() >= 20 && !st.isCompleted())
		{
			st.startQuest();
			return "seer_moirase_q0122_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case KARUDA:
				if(reply == 3 && cond == 1)
				{
					st.giveAdena(8923, true);
					st.addExpAndSp(45151, 2310);
					st.exitQuest(QuestType.ONE_TIME);
					return "karuda_q0122_0201.htm";
				}
				break;
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
			case MOIRA:
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 20)
					{
						return "seer_moirase_q0122_0101.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "seer_moirase_q0122_0103.htm";
					}
				}
				else
				{
					return "seer_moirase_q0122_0105.htm";
				}
			case KARUDA:
				if(cond == 1)
				{
					return "karuda_q0122_0101.htm";
				}
				break;
		}
		return null;
	}
}