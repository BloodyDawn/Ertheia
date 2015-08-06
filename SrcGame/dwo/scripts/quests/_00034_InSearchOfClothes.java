package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00034_InSearchOfClothes extends Quest
{
	private static final int SPINNERET = 7528;
	private static final int ARMOR_PART = 34991;
	private static final int JEWEL_PART = 34992;
	private static final int SPIDERSILK = 1493;
	private static final int MYSTERIOUS_CLOTH = 7076;

	public _00034_InSearchOfClothes()
	{
		addStartNpc(30088);
		addTalkId(30088, 30165, 30294);
		addKillId(20560);
		questItemIds = new int[]{SPINNERET};
	}

	public static void main(String[] args)
	{
		new _00034_InSearchOfClothes();
	}

	@Override
	public int getQuestId()
	{
		return 34;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "30088-1.htm":
				st.startQuest();
				break;
			case "30294-1.htm":
				st.setCond(2);
				break;
			case "30088-3.htm":
				st.setCond(3);
				break;
			case "30165-1.htm":
				st.setCond(4);
				break;
			case "30165-3.htm":
				if(st.getQuestItemsCount(SPINNERET) == 10)
				{
					st.takeItems(SPINNERET, 10);
					st.giveItems(SPIDERSILK, 1);
					st.setCond(6);
				}
				else
				{
					htmltext = "30165-1r.htm";
				}
				break;
			case "30088-5.htm":
				if(st.getQuestItemsCount(ARMOR_PART) >= 900 && st.getQuestItemsCount(JEWEL_PART) >= 500 && st.hasQuestItems(SPIDERSILK))
				{
					st.takeItems(ARMOR_PART, 900);
					st.takeItems(JEWEL_PART, 500);
					st.takeItems(SPIDERSILK, 1);
					st.giveItems(MYSTERIOUS_CLOTH, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
				}
				else
				{
					htmltext = "30088-havent.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(st.getQuestItemsCount(SPINNERET) < 10)
		{
			st.giveItems(SPINNERET, 1);
			if(st.getQuestItemsCount(SPINNERET) == 10)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(5);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30088)
		{
			switch(cond)
			{
				case 0:
					if(st.getQuestItemsCount(MYSTERIOUS_CLOTH) == 0)
					{
						if(st.getPlayer().getLevel() >= 60)
						{
							QuestState fwear = st.getPlayer().getQuestState(_00037_PleaseMakeMeFormalWear.class);
							if(fwear != null && fwear.getCond() == 6)
							{
								htmltext = "30088-0.htm";
							}
							else
							{
								st.exitQuest(QuestType.REPEATABLE);
							}
						}
						else
						{
							htmltext = "30088-6.htm";
						}
					}
					break;
				case 1:
					htmltext = "30088-1r.htm";
					break;
				case 2:
					htmltext = "30088-2.htm";
					break;
				case 3:
					htmltext = "30088-3r.htm";
					break;
				case 6:
					htmltext = st.getQuestItemsCount(ARMOR_PART) < 900 || st.getQuestItemsCount(JEWEL_PART) < 500 || st.getQuestItemsCount(SPIDERSILK) < 1 ? "30088-havent.htm" : "30088-4.htm";
					break;
			}
		}
		else if(npcId == 30294)
		{
			switch(cond)
			{
				case 1:
					htmltext = "30294-0.htm";
					break;
				case 2:
					htmltext = "30294-1r.htm";
					break;
			}
		}
		else if(npcId == 30165)
		{
			switch(cond)
			{
				case 3:
					htmltext = "30165-0.htm";
					break;
				case 4:
					if(st.getQuestItemsCount(SPINNERET) < 10)
					{
						htmltext = "30165-1r.htm";
					}
					break;
				case 5:
					htmltext = "30165-2.htm";
					break;
				case 6:
					if(st.getQuestItemsCount(ARMOR_PART) < 900 || st.getQuestItemsCount(JEWEL_PART) < 500 || st.getQuestItemsCount(SPIDERSILK) < 1)
					{
						htmltext = "30165-3r.htm";
					}
					break;
			}
		}
		return htmltext;
	}
}