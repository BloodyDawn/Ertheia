package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00017_LightAndDarkness extends Quest
{
	public _00017_LightAndDarkness()
	{
		addStartNpc(31517);
		addTalkId(31508, 31509, 31510, 31511);
		questItemIds = new int[]{7168};
	}

	public static void main(String[] args)
	{
		new _00017_LightAndDarkness();
	}

	@Override
	public int getQuestId()
	{
		return 17;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		switch(event)
		{
			case "31517-04.htm":
				st.startQuest();
				st.giveItems(7168, 4);
				break;
			case "31508-02.htm":
				st.takeItems(7168, 1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31509-02.htm":
				st.takeItems(7168, 1);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31510-02.htm":
				st.takeItems(7168, 1);
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31511-02.htm":
				st.takeItems(7168, 1);
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
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
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 61)
				{
					htmltext = "31517-02.htm";
				}
				else
				{
					htmltext = "31517-03.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(cond > 0 && cond < 5 && st.hasQuestItems(7168))
			{
				htmltext = "31517-05.htm";
			}
			else if(cond > 0 && cond < 5 && !st.hasQuestItems(7168))
			{
				htmltext = "31517-06.htm";
				st.setCond(0);
				st.exitQuest(QuestType.ONE_TIME);
			}
			else if(cond == 5 && !st.hasQuestItems(7168))
			{
				htmltext = "31517-07.htm";
				st.addExpAndSp(105527, 0);
				st.setCond(0);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
			}
		}
		else if(npcId == 31508)
		{
			if(cond == 1)
			{
				htmltext = st.hasQuestItems(7168) ? "31508-01.htm" : "31508-03.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31508-04.htm";
			}
		}
		else if(npcId == 31509)
		{
			if(cond == 2)
			{
				htmltext = st.hasQuestItems(7168) ? "31509-01.htm" : "31509-03.htm";
			}
			else if(cond == 3)
			{
				htmltext = "31509-04.htm";
			}
		}
		else if(npcId == 31510)
		{
			if(cond == 3)
			{
				htmltext = st.hasQuestItems(7168) ? "31510-01.htm" : "31510-03.htm";
			}
			else if(cond == 4)
			{
				htmltext = "31510-04.htm";
			}
		}
		else if(npcId == 31511)
		{
			if(cond == 4)
			{
				htmltext = st.hasQuestItems(7168) ? "31511-01.htm" : "31511-03.htm";
			}
			else if(cond == 5)
			{
				htmltext = "31511-04.htm";
			}
		}
		return htmltext;
	}
}