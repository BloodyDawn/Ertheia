package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00381_LetsBecomeARoyalMember extends Quest
{
	// Квестовые предметы
	private static int KAILS_COIN = 5899;
	private static int COIN_ALBUM = 5900;
	private static int MEMBERSHIP_1 = 3813;
	private static int CLOVER_COIN = 7569;
	private static int ROYAL_MEMBERSHIP = 5898;

	// Квестовые персонажи
	private static int SORINT = 30232;
	private static int SANDRA = 30090;

	// Квестовые монстры
	private static int ANCIENT_GARGOYLE = 21018;
	private static int VEGUS = 27316;

	// Шансы выпадения предметов
	private static int GARGOYLE_CHANCE = 5;
	private static int VEGUS_CHANCE = 100;

	public _00381_LetsBecomeARoyalMember()
	{
		addStartNpc(SORINT);
		addTalkId(SORINT, SANDRA);
		addKillId(ANCIENT_GARGOYLE, VEGUS);
		questItemIds = new int[]{KAILS_COIN, COIN_ALBUM, CLOVER_COIN};
	}

	public static void main(String[] args)
	{
		new _00381_LetsBecomeARoyalMember();
	}

	@Override
	public int getQuestId()
	{
		return 381;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("30232-02.htm"))
		{
			if(st.getPlayer().getLevel() >= 55 && st.getQuestItemsCount(MEMBERSHIP_1) > 0)
			{
				st.startQuest();
				return "30232-03.htm";
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "30232-02.htm";
			}
		}
		if(event.equalsIgnoreCase("30090-02.htm"))
		{
			if(st.getCond() == 1)
			{
				st.set("id", "1");
				st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		int npcId = npc.getNpcId();

		long album = st.getQuestItemsCount(COIN_ALBUM);
		long coin = st.getQuestItemsCount(KAILS_COIN);
		long clover = st.getQuestItemsCount(CLOVER_COIN);

		if(npcId == ANCIENT_GARGOYLE && coin == 0)
		{
			if(Rnd.getChance(GARGOYLE_CHANCE))
			{
				st.giveItems(KAILS_COIN, 1);
				if(album > 0 || clover > 0)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		else if(npcId == VEGUS && clover + album == 0 && st.getInt("id") != 0)
		{
			if(Rnd.getChance(VEGUS_CHANCE))
			{
				st.giveItems(CLOVER_COIN, 1);
				if(coin > 0)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		long album = st.getQuestItemsCount(COIN_ALBUM);

		if(npcId == SORINT)
		{
			if(cond == 0)
			{
				return "30232-01.htm";
			}
			else if(cond == 1)
			{
				long coin = st.getQuestItemsCount(KAILS_COIN);
				if(coin > 0 && album > 0)
				{
					if(!st.takeItemsAndConfirm(KAILS_COIN, -1))
					{
						return "<html><body>Неверное количество предметов.</body></html>";
					}
					if(!st.takeItemsAndConfirm(COIN_ALBUM, -1))
					{
						return "<html><body>Неверное количество предметов.</body></html>";
					}
					st.giveItems(ROYAL_MEMBERSHIP, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "30232-06.htm";
				}
				else if(album == 0)
				{
					return "30232-05.htm";
				}
				else if(coin == 0)
				{
					return "30232-04.htm";
				}
			}
		}
		else
		{
			long clover = st.getQuestItemsCount(CLOVER_COIN);
			if(album > 0)
			{
				return "30090-05.htm";
			}
			else if(clover > 0)
			{
				if(!st.takeItemsAndConfirm(CLOVER_COIN, -1))
				{
					return "<html><body>Неверное количество предметов.</body></html>";
				}
				st.giveItems(COIN_ALBUM, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				return "30090-04.htm";
			}
			else
			{
				return st.getInt("id") == 0 ? "30090-01.htm" : "30090-03.htm";
			}
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return !(player.getLevel() < 55 && player.getInventory().getCountOf(MEMBERSHIP_1) < 1);
	}
}