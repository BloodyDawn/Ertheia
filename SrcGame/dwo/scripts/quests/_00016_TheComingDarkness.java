package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00016_TheComingDarkness extends Quest
{
	//npc
	public static final int HIERARCH = 31517;
	//items
	public static final int CRYSTAL_OF_SEAL = 7167;
	//ALTAR_LIST (MOB_ID, cond)
	public final int[][] ALTAR_LIST = {{31512, 1}, {31513, 2}, {31514, 3}, {31515, 4}, {31516, 5}};

	public _00016_TheComingDarkness()
	{
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH);
		for(int[] element : ALTAR_LIST)
		{
			addTalkId(element[0]);
		}

		questItemIds = new int[]{CRYSTAL_OF_SEAL};
	}

	public static void main(String[] args)
	{
		new _00016_TheComingDarkness();
	}

	@Override
	public int getQuestId()
	{
		return 16;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equalsIgnoreCase("31517-02.htm"))
		{
			st.startQuest();
			st.giveItems(CRYSTAL_OF_SEAL, 5);
		}
		for(int[] element : ALTAR_LIST)
		{
			if(event.equalsIgnoreCase(element[0] + "-02.htm"))
			{
				st.takeItems(CRYSTAL_OF_SEAL, 1);
				st.setCond(element[1] + 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31517)
		{
			if(cond < 1)
			{
				if(st.getPlayer().getLevel() < 61)
				{
					htmltext = "31517-00.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
				else
				{
					htmltext = "31517-01.htm";
				}
			}
			else if(cond > 0 && cond < 6 && st.hasQuestItems(CRYSTAL_OF_SEAL))
			{
				htmltext = "31517-02r.htm";
			}
			else if(cond > 0 && cond < 6 && !st.hasQuestItems(CRYSTAL_OF_SEAL))
			{
				htmltext = "31517-proeb.htm";
				st.exitQuest(QuestType.ONE_TIME);
			}
			else if(cond > 5 && !st.hasQuestItems(CRYSTAL_OF_SEAL))
			{
				htmltext = "31517-03.htm";
				st.addExpAndSp(221958, 0);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
			}
		}
		for(int[] element : ALTAR_LIST)
		{
			if(npcId == element[0])
			{
				if(cond == element[1])
				{
					htmltext = st.hasQuestItems(CRYSTAL_OF_SEAL) ? element[0] + "-01.htm" : element[0] + "-03.htm";
				}
				else if(cond == element[1] + 1)
				{
					htmltext = element[0] + "-04.htm";
				}
			}
		}
		return htmltext;
	}
}