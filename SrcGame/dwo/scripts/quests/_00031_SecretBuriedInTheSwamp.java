package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00031_SecretBuriedInTheSwamp extends Quest
{
	int ABERCROMBIE = 31555;
	int FORGOTTEN_MONUMENT_1 = 31661;
	int FORGOTTEN_MONUMENT_2 = 31662;
	int FORGOTTEN_MONUMENT_3 = 31663;
	int FORGOTTEN_MONUMENT_4 = 31664;
	int CORPSE_OF_DWARF = 31665;

	int KRORINS_JOURNAL = 7252;

	public _00031_SecretBuriedInTheSwamp()
	{
		addStartNpc(ABERCROMBIE);

		for(int i = 31661; i <= 31665; i++)
		{
			addTalkId(i);
		}

		questItemIds = new int[]{KRORINS_JOURNAL};
	}

	public static void main(String[] args)
	{
		new _00031_SecretBuriedInTheSwamp();
	}

	@Override
	public int getQuestId()
	{
		return 31;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		switch(event)
		{
			case "31555-1.htm":
				st.startQuest();
				break;
			case "31665-1.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.giveItems(KRORINS_JOURNAL, 1);
				break;
			case "31555-4.htm":
				st.setCond(3);
				break;
			case "31661-1.htm":
				st.setCond(4);
				break;
			case "31662-1.htm":
				st.setCond(5);
				break;
			case "31663-1.htm":
				st.setCond(6);
				break;
			case "31664-1.htm":
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31555-7.htm":
				st.takeItems(KRORINS_JOURNAL, -1);
				st.addExpAndSp(130000, 0);
				st.giveAdena(40000, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
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
		if(npcId == ABERCROMBIE)
		{
			switch(cond)
			{
				case 0:
					if(st.getPlayer().getLevel() >= 66)
					{
						htmltext = "31555-0.htm";
					}
					else
					{
						htmltext = "31555-0a.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
					break;
				case 1:
					htmltext = "31555-2.htm";
					break;
				case 2:
					htmltext = "31555-3.htm";
					break;
				case 3:
					htmltext = "31555-5.htm";
					break;
				case 7:
					htmltext = "31555-6.htm";
					break;
			}
		}
		else if(npcId == CORPSE_OF_DWARF)
		{
			if(cond == 1)
			{
				htmltext = "31665-0.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31665-2.htm";
			}
		}
		else if(npcId == FORGOTTEN_MONUMENT_1)
		{
			if(cond == 3)
			{
				htmltext = "31661-0.htm";
			}
			else if(cond > 3)
			{
				htmltext = "31661-2.htm";
			}
		}
		else if(npcId == FORGOTTEN_MONUMENT_2)
		{
			if(cond == 4)
			{
				htmltext = "31662-0.htm";
			}
			else if(cond > 4)
			{
				htmltext = "31662-2.htm";
			}
		}
		else if(npcId == FORGOTTEN_MONUMENT_3)
		{
			if(cond == 5)
			{
				htmltext = "31663-0.htm";
			}
			else if(cond > 5)
			{
				htmltext = "31663-2.htm";
			}
		}
		else if(npcId == FORGOTTEN_MONUMENT_4)
		{
			if(cond == 6)
			{
				htmltext = "31664-0.htm";
			}
			else if(cond > 6)
			{
				htmltext = "31664-2.htm";
			}
		}
		return htmltext;
	}
}