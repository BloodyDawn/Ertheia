package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00033_MakeAPairOfDressShoes extends Quest
{
	int LEATHER = 1882;
	int JEWEL_PART = 34992;
	int DRESS_SHOES_BOX = 7113;

	public _00033_MakeAPairOfDressShoes()
	{
		addStartNpc(30838);
		addTalkId(30838, 30164, 31520);
	}

	public static void main(String[] args)
	{
		new _00033_MakeAPairOfDressShoes();
	}

	@Override
	public int getQuestId()
	{
		return 33;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "30838-1.htm":
				st.startQuest();
				break;
			case "31520-1.htm":
				st.setCond(2);
				break;
			case "30838-3.htm":
				st.setCond(3);
				break;
			case "30838-5.htm":
				if(st.getQuestItemsCount(LEATHER) >= 200 && st.getQuestItemsCount(JEWEL_PART) >= 60 && st.getQuestItemsCount(PcInventory.ADENA_ID) >= 200000)
				{
					st.takeItems(LEATHER, 200);
					st.takeItems(JEWEL_PART, 60);
					st.takeAdena(200000);
					st.setCond(4);
				}
				else
				{
					htmltext = "У Вас недостаточно материалов";
				}
				break;
			case "30164-1.htm":
				if(st.getQuestItemsCount(PcInventory.ADENA_ID) >= 300000)
				{
					st.takeAdena(300000);
					st.setCond(5);
				}
				else
				{
					htmltext = "30164-havent.htm";
				}
				break;
			case "30838-7.htm":
				st.giveItems(DRESS_SHOES_BOX, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.REPEATABLE);
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30838)
		{
			if(cond == 0 && !st.hasQuestItems(DRESS_SHOES_BOX))
			{
				if(st.getPlayer().getLevel() >= 60)
				{
					QuestState fwear = st.getPlayer().getQuestState(_00037_PleaseMakeMeFormalWear.class);
					if(fwear != null && fwear.isStarted())
					{
						if(fwear.getCond() == 7)
						{
							htmltext = "30838-0.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else
				{
					htmltext = "30838-0.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "30838-1.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30838-2.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(LEATHER) >= 200 && st.getQuestItemsCount(JEWEL_PART) >= 60 && st.getQuestItemsCount(PcInventory.ADENA_ID) >= 200000)
			{
				htmltext = "30838-4.htm";
			}
			else if(cond == 3 && (st.getQuestItemsCount(LEATHER) < 200 || st.getQuestItemsCount(JEWEL_PART) < 60 || st.getQuestItemsCount(PcInventory.ADENA_ID) < 200000))
			{
				htmltext = "30838-4r.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30838-5r.htm";
			}
			else if(cond == 5)
			{
				htmltext = "30838-6.htm";
			}
		}
		else if(npcId == 31520)
		{
			if(cond == 1)
			{
				htmltext = "31520-0.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31520-1r.htm";
			}
		}
		else if(npcId == 30164)
		{
			if(cond == 4)
			{
				htmltext = "30164-0.htm";
			}
			else if(cond == 5)
			{
				htmltext = "30164-2.htm";
			}
		}
		return htmltext;
	}
}