package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00037_PleaseMakeMeFormalWear extends Quest
{
	int MYSTERIOUS_CLOTH = 7076;
	int JEWEL_BOX = 7077;
	int SEWING_KIT = 7078;
	int DRESS_SHOES_BOX = 7113;
	int FORMAL_WEAR = 6408;
	int SIGNET_RING = 7164;
	int ICE_WINE = 7160;
	int BOX_OF_COOKIES = 7159;

	public _00037_PleaseMakeMeFormalWear()
	{
		addStartNpc(30842);
		addTalkId(30842, 31520, 31521, 31627);
	}

	public static void main(String[] args)
	{
		new _00037_PleaseMakeMeFormalWear();
	}

	@Override
	public int getQuestId()
	{
		return 37;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "30842-1.htm":
				st.startQuest();
				break;
			case "31520-1.htm":
				st.giveItems(SIGNET_RING, 1);
				st.setCond(2);
				break;
			case "31521-1.htm":
				st.takeItems(SIGNET_RING, 1);
				st.giveItems(ICE_WINE, 1);
				st.setCond(3);
				break;
			case "31627-1.htm":
				if(st.hasQuestItems(ICE_WINE))
				{
					st.takeItems(ICE_WINE, 1);
					st.setCond(4);
				}
				else
				{
					htmltext = "У Вас нет нужных материалов!";
				}
				break;
			case "31521-3.htm":
				st.giveItems(BOX_OF_COOKIES, 1);
				st.setCond(5);
				break;
			case "31520-3.htm":
				st.takeItems(BOX_OF_COOKIES, 1);
				st.setCond(6);
				break;
			case "31520-5.htm":
				st.takeItems(MYSTERIOUS_CLOTH, 1);
				st.takeItems(JEWEL_BOX, 1);
				st.takeItems(SEWING_KIT, 1);
				st.setCond(7);
				break;
			case "31520-7.htm":
				if(st.hasQuestItems(DRESS_SHOES_BOX))
				{
					st.takeItems(DRESS_SHOES_BOX, 1);
					st.giveItems(FORMAL_WEAR, 1);
					st.unset("cond");
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else
				{
					htmltext = "У Вас нет нужных материалов!";
				}
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
		if(npcId == 30842)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
				{
					htmltext = "30842-0.htm";
				}
				else
				{
					htmltext = "30842-2.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(cond == 1)
			{
				htmltext = "30842-1.htm";
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
				htmltext = "31520-1.htm";
			}
			else if(cond == 5 || cond == 6)
			{
				if(st.hasQuestItems(MYSTERIOUS_CLOTH) && st.hasQuestItems(JEWEL_BOX) && st.hasQuestItems(SEWING_KIT))
				{
					htmltext = "31520-4.htm";
				}
				else
				{
					htmltext = st.hasQuestItems(BOX_OF_COOKIES) ? "31520-2.htm" : "31520-3.htm";
				}
			}
			else if(cond == 7)
			{
				htmltext = st.hasQuestItems(DRESS_SHOES_BOX) ? "31520-6.htm" : "31520-5.htm";
			}
		}
		else if(npcId == 31521)
		{
			if(st.hasQuestItems(SIGNET_RING))
			{
				htmltext = "31521-0.htm";
			}
			else if(cond == 3)
			{
				htmltext = "31521-1.htm";
			}
			else if(cond == 4)
			{
				htmltext = "31521-2.htm";
			}
			else if(cond == 5)
			{
				htmltext = "31521-3.htm";
			}
		}
		else if(npcId == 31627)
		{
			htmltext = "31627-0.htm";
		}
		return htmltext;
	}
}