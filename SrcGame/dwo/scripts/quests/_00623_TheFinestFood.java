package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.01.12
 * Time: 5:54
 */

public class _00623_TheFinestFood extends Quest
{
	// Квестовые персонажи
	private static final int JEREMY = 31521;

	// Квестовые предметы
	private static final int LEAF_OF_FLAVA = 7199;
	private static final int BUFFALO_MEAT = 7200;
	private static final int ANTELOPE_HORN = 7201;

	// Квестовые монстры
	private static final int BUFFALO = 21315;
	private static final int FLAVA = 21316;
	private static final int ANTELOPE = 21318;

	public _00623_TheFinestFood()
	{

		addStartNpc(JEREMY);
		addTalkId(JEREMY);
		addKillId(BUFFALO, FLAVA, ANTELOPE);
		questItemIds = new int[]{LEAF_OF_FLAVA, BUFFALO_MEAT, ANTELOPE_HORN};
	}

	public static void main(String[] args)
	{
		new _00623_TheFinestFood();
	}

	@Override
	public int getQuestId()
	{
		return 623;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		int cond = st.getCond();
		if(event.equalsIgnoreCase("31521-03.htm") && cond == 0)
		{
			if(st.getPlayer().getLevel() >= 71)
			{
				st.startQuest();
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "31521-02.htm";
			}
		}
		else if(event.equalsIgnoreCase("31521-07.htm"))
		{
			if(cond == 2 && summ(st) >= 300)
			{
				st.takeItems(LEAF_OF_FLAVA, -1);
				st.takeItems(BUFFALO_MEAT, -1);
				st.takeItems(ANTELOPE_HORN, -1);
				st.giveItems(PcInventory.ADENA_ID, 73000);
				st.addExpAndSp(230000, 18250);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.REPEATABLE);
				return "31521-06.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		if(cond == 1) // Like off C4 PTS AI (убрали  && Rnd.chance(50))
		{
			if(npcId == BUFFALO)
			{
				if(st.getQuestItemsCount(BUFFALO_MEAT) < 100)
				{
					st.giveItems(BUFFALO_MEAT, 1);
					if(st.getQuestItemsCount(BUFFALO_MEAT) == 100)
					{
						if(summ(st) >= 300)
						{
							st.setCond(2);
						}
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
			else if(npcId == FLAVA)
			{
				if(st.getQuestItemsCount(LEAF_OF_FLAVA) < 100)
				{
					st.giveItems(LEAF_OF_FLAVA, 1);
					if(st.getQuestItemsCount(LEAF_OF_FLAVA) == 100)
					{
						if(summ(st) >= 300)
						{
							st.setCond(2);
						}
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
			else if(npcId == ANTELOPE)
			{
				if(st.getQuestItemsCount(ANTELOPE_HORN) < 100)
				{
					st.giveItems(ANTELOPE_HORN, 1);
					if(st.getQuestItemsCount(ANTELOPE_HORN) == 100)
					{
						if(summ(st) >= 300)
						{
							st.setCond(2);
						}
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 0)
		{
			return "31521-01.htm";
		}
		if(st.getState() == STARTED)
		{
			if(cond == 1)
			{
				return "31521-05.htm";
			}
			else if(cond == 2 && summ(st) >= 300)
			{
				return "31521-04.htm";
			}
		}
		return null;
	}

	private long summ(QuestState st)
	{
		return st.getQuestItemsCount(LEAF_OF_FLAVA) + st.getQuestItemsCount(BUFFALO_MEAT) + st.getQuestItemsCount(ANTELOPE_HORN);
	}
}
