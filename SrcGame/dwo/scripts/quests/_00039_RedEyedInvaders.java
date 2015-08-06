package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00039_RedEyedInvaders extends Quest
{
	int BBN = 7178;
	int RBN = 7179;
	int IP = 7180;
	int GML = 7181;
	int[] REW = {6521, 6529, 6535};

	public _00039_RedEyedInvaders()
	{
		addStartNpc(30334);
		addTalkId(30332);
		addKillId(20919, 20920, 20921, 20925);
		questItemIds = new int[]{BBN, IP, RBN, GML};
	}

	public static void main(String[] args)
	{
		new _00039_RedEyedInvaders();
	}

	@Override
	public int getQuestId()
	{
		return 39;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "30334-02.htm":
				st.startQuest();
				break;
			case "30332-02.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
				break;
			case "30332-04.htm":
				if(st.getQuestItemsCount(BBN) == 100 && st.getQuestItemsCount(RBN) == 100)
				{
					st.setCond(4);
					st.takeItems(BBN, -1);
					st.takeItems(RBN, -1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
				}
				else
				{
					htmltext = "30332-02r.htm";
				}
				break;
			case "30332-06.htm":
				if(st.getQuestItemsCount(IP) == 30 && st.getQuestItemsCount(GML) == 30)
				{
					st.addExpAndSp(62236, 2783);
					st.giveItems(REW[0], 60);
					st.giveItems(REW[1], 1);
					st.giveItems(REW[2], 500);
					st.takeItems(IP, -1);
					st.takeItems(GML, -1);
					st.setCond(0);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else
				{
					htmltext = "30332-04r.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2 && Rnd.getChance(60))
		{
			if((npcId == 20919 || npcId == 20920) && st.getQuestItemsCount(BBN) <= 99)
			{
				st.giveItems(BBN, 1);
			}
			else if(npcId == 20921 && st.getQuestItemsCount(RBN) <= 99)
			{
				st.giveItems(RBN, 1);
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if(st.getQuestItemsCount(BBN) + st.getQuestItemsCount(RBN) == 200)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}

		if(cond == 4 && Rnd.getChance(60))
		{
			if((npcId == 20920 || npcId == 20921) && st.getQuestItemsCount(IP) <= 29)
			{
				st.giveItems(IP, 1);
			}
			else if(npcId == 20925 && st.getQuestItemsCount(GML) <= 29)
			{
				st.giveItems(GML, 1);
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if(st.getQuestItemsCount(IP) + st.getQuestItemsCount(GML) == 60)
			{
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int cond = st.getCond();
		if(npc.getNpcId() == 30334)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 20)
				{
					htmltext = "30334-00.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
				else if(st.getPlayer().getLevel() >= 20)
				{
					htmltext = "30334-01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "30334-02r.htm";
			}
		}
		else if(npc.getNpcId() == 30332)
		{
			if(cond == 1)
			{
				htmltext = "30332-01.htm";
			}
			else if(cond == 2 && (st.getQuestItemsCount(BBN) < 100 || st.getQuestItemsCount(RBN) < 100))
			{
				htmltext = "30332-02r.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(BBN) == 100 && st.getQuestItemsCount(RBN) == 100)
			{
				htmltext = "30332-03.htm";
			}
			else if(cond == 4 && (st.getQuestItemsCount(IP) < 30 || st.getQuestItemsCount(GML) < 30))
			{
				htmltext = "30332-04r.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(IP) == 30 && st.getQuestItemsCount(GML) == 30)
			{
				htmltext = "30332-05.htm";
			}
		}
		return htmltext;
	}
}