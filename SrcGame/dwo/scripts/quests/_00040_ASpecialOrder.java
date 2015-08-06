package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00040_ASpecialOrder extends Quest
{
	// NPC
	static final int HELVETIA = 30081;
	static final int OFULLE = 31572;
	static final int GESTO = 30511;

	// Items
	static final int OrangeNimbleFish = 6450;
	static final int OrangeUglyFish = 6451;
	static final int OrangeFatFish = 6452;
	static final int FishChest = 12764;
	static final int GoldenCobol = 5079;
	static final int ThornCobol = 5082;
	static final int GreatCobol = 5084;
	static final int SeedJar = 12765;
	static final int WondrousCubic = 10632;
	static final int[] QUESTITEMS = {12764, 12765};

	public _00040_ASpecialOrder()
	{
		addStartNpc(HELVETIA);
		addTalkId(HELVETIA, OFULLE, GESTO);
		questItemIds = QUESTITEMS;
	}

	public static void main(String[] args)
	{
		new _00040_ASpecialOrder();
	}

	@Override
	public int getQuestId()
	{
		return 40;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;

		switch(event)
		{
			case "30081-02.htm":
				st.setCond(1);
				if(Rnd.getChance(50))
				{
					st.setCond(2);
					htmltext = "30081-02a.htm";
				}
				else
				{
					st.setCond(5);
					htmltext = "30081-02b.htm";
				}
				st.setState(STARTED);
				break;
			case "30511-03.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31572-03.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30081-05a.htm":
				st.takeItems(FishChest, 1);
				st.giveItems(WondrousCubic, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "30081-05b.htm":
				st.takeItems(SeedJar, 1);
				st.giveItems(WondrousCubic, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		QuestStateType id = st.getState();
		int cond = st.getCond();
		if(id == COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		else if(npcId == HELVETIA)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 40)
				{
					htmltext = "30081-01.htm";
				}
				else
				{
					htmltext = "30081-00.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(cond == 2 || cond == 3)
			{
				htmltext = "30081-03a.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30081-04a.htm";
			}
			else if(cond == 5 || cond == 6)
			{
				htmltext = "30081-03b.htm";
			}
			else if(cond == 7)
			{
				htmltext = "30081-04b.htm";
			}
		}
		else if(npcId == OFULLE)
		{
			if(cond == 2)
			{
				htmltext = "31572-01.htm";
			}
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(OrangeNimbleFish) >= 10 && st.getQuestItemsCount(OrangeUglyFish) >= 10 && st.getQuestItemsCount(OrangeFatFish) >= 10)
				{
					st.setCond(4);
					st.takeItems(OrangeNimbleFish, 10);
					st.takeItems(OrangeUglyFish, 10);
					st.takeItems(OrangeFatFish, 10);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(FishChest, 1);
					htmltext = "31572-04.htm";
				}
				else
				{
					htmltext = "31572-05.htm";
				}
			}
			else if(cond == 4)
			{
				htmltext = "31572-06.htm";
			}
		}
		else if(npcId == GESTO)
		{
			if(cond == 5)
			{
				htmltext = "30511-01.htm";
			}
			else if(cond == 6)
			{
				if(st.getQuestItemsCount(GoldenCobol) >= 40 && st.getQuestItemsCount(ThornCobol) >= 40 && st.getQuestItemsCount(GreatCobol) >= 40)
				{
					st.setCond(7);
					st.takeItems(GoldenCobol, 40);
					st.takeItems(ThornCobol, 40);
					st.takeItems(GreatCobol, 40);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(SeedJar, 1);
					htmltext = "30511-04.htm";
				}
				else
				{
					htmltext = "30511-05.htm";
				}
			}
			else if(cond == 7)
			{
				htmltext = "30511-06.htm";
			}
		}
		return htmltext;
	}
}