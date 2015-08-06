package dwo.scripts.quests;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.01.12
 * Time: 5:54
 */

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00621_EggDelivery extends Quest
{
	// Квестовые персонажи
	private static final int JEREMY = 31521;
	private static final int PULIN = 31543;
	private static final int NAFF = 31544;
	private static final int CROCUS = 31545;
	private static final int KUBER = 31546;
	private static final int BEORIN = 31547;
	private static final int VALENTINE = 31584;

	// Квестовые предметы
	private static final int BOILED_EGGS = 7195;
	private static final int FEE_OF_EGGS = 7196;

	// Награды
	private static final int HASTE_POTION = 734;
	private static final int RecipeSealedTateossianRing = 6849;
	private static final int RecipeSealedTateossianEarring = 6847;
	private static final int RecipeSealedTateossianNecklace = 6851;

	// Шанс получить рецепт
	private static final int RPCHANCE = 10;

	// Выставить в 1, если нужно выдавать 100% рецепты.
	// TODO:Сделать во всех квестах c рецептами и вывести в конфиг
	private static final boolean ALT_RP100 = false;

	public _00621_EggDelivery()
	{

		addStartNpc(JEREMY);
		addTalkId(JEREMY);
		addTalkId(PULIN);
		addTalkId(NAFF);
		addTalkId(CROCUS);
		addTalkId(KUBER);
		addTalkId(BEORIN);
		addTalkId(VALENTINE);

		questItemIds = new int[]{BOILED_EGGS, FEE_OF_EGGS};
	}

	public static void main(String[] args)
	{
		new _00621_EggDelivery();
	}

	@Override
	public int getQuestId()
	{
		return 621;
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

		switch(event)
		{
			case "31521-1.htm":
				if(cond == 0)
				{
					st.startQuest();
					st.giveItems(BOILED_EGGS, 5);
				}
				else
				{
					return getNoQuestMsg(player);
				}
				break;
			case "31543-1.htm":
				if(st.hasQuestItems(BOILED_EGGS))
				{
					if(cond == 1)
					{
						st.takeItems(BOILED_EGGS, 1);
						st.giveItems(FEE_OF_EGGS, 1);
						st.setCond(2);
					}
					else
					{
						return getNoQuestMsg(player);
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
			case "31544-1.htm":
				if(st.hasQuestItems(BOILED_EGGS))
				{
					if(cond == 2)
					{
						st.takeItems(BOILED_EGGS, 1);
						st.giveItems(FEE_OF_EGGS, 1);
						st.setCond(3);
					}
					else
					{
						return getNoQuestMsg(player);
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
			case "31545-1.htm":
				if(st.hasQuestItems(BOILED_EGGS))
				{
					if(cond == 3)
					{
						st.takeItems(BOILED_EGGS, 1);
						st.giveItems(FEE_OF_EGGS, 1);
						st.setCond(4);
					}
					else
					{
						return getNoQuestMsg(player);
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
			case "31546-1.htm":
				if(st.hasQuestItems(BOILED_EGGS))
				{
					if(cond == 4)
					{
						st.takeItems(BOILED_EGGS, 1);
						st.giveItems(FEE_OF_EGGS, 1);
						st.setCond(5);
					}
					else
					{
						return getNoQuestMsg(player);
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
			case "31547-1.htm":
				if(st.hasQuestItems(BOILED_EGGS))
				{
					if(cond == 5)
					{
						st.takeItems(BOILED_EGGS, 1);
						st.giveItems(FEE_OF_EGGS, 1);
						st.setCond(6);
					}
					else
					{
						return getNoQuestMsg(player);
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
			case "31521-3.htm":
				st.setCond(7);
				break;
			case "31584-2.htm":
				if(st.getQuestItemsCount(FEE_OF_EGGS) == 5)
				{
					st.takeItems(FEE_OF_EGGS, 5);
					if(Rnd.getChance(RPCHANCE))
					{
						if(Rnd.getChance(40))
						{
							st.giveItems(RecipeSealedTateossianRing + (ALT_RP100 ? 1 : 0), 1);
						}
						else if(Rnd.getChance(40))
						{
							st.giveItems(RecipeSealedTateossianEarring + (ALT_RP100 ? 1 : 0), 1);
						}
						else
						{
							st.giveItems(RecipeSealedTateossianNecklace + (ALT_RP100 ? 1 : 0), 1);
						}
					}
					else
					{
						st.giveAdena(18800, true);
						st.giveItems(HASTE_POTION, 1);
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
				}
				else
				{
					return getNoQuestMsg(player);
				}
				break;
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(st.isCreated())
		{
			st.setCond(0);
		}
		if(npcId == 31521 && cond == 0)
		{
			if(player.getLevel() >= 68)
			{
				return "31521-0.htm";
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "31521-nolvl.htm";
			}
		}
		if(st.isStarted())
		{
			if(npcId == 31543 && cond == 1 && st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				return "31543-0.htm";
			}
			else if(npcId == 31544 && cond == 2 && st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				return "31544-0.htm";
			}
			else if(npcId == 31545 && cond == 3 && st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				return "31545-0.htm";
			}
			else if(npcId == 31546 && cond == 4 && st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				return "31546-0.htm";
			}
			else if(npcId == 31547 && cond == 5 && st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				return "31547-0.htm";
			}
			else if(npcId == 31521 && cond == 6 && st.getQuestItemsCount(FEE_OF_EGGS) == 5)
			{
				return "31521-2.htm";
			}
			else if(npcId == 31521 && cond == 7 && st.getQuestItemsCount(FEE_OF_EGGS) == 5)
			{
				return "31521-4.htm";
			}
			else if(npcId == 31584 && cond == 7 && st.getQuestItemsCount(FEE_OF_EGGS) == 5)
			{
				return "31584-1.htm";
			}
		}
		return null;
	}
}
