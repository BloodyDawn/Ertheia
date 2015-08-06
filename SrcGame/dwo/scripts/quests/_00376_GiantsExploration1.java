package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.math.NumberUtils;

public class _00376_GiantsExploration1 extends Quest
{
	// NPC
	private static final int SOBLING = 31147;

	// Items
	private static final int ANCIENT_PARCHMENT = 14841;
	private static final int BOOK1 = 14836;
	private static final int BOOK2 = 14837;
	private static final int BOOK3 = 14838;
	private static final int BOOK4 = 14839;
	private static final int BOOK5 = 14840;

	private static final TIntObjectHashMap<Integer> Drops = new TIntObjectHashMap<>();

	public _00376_GiantsExploration1()
	{
		addStartNpc(SOBLING);
		addTalkId(SOBLING);

		// Droplist: mobId, chance
		Drops.put(22670, 40);
		Drops.put(22671, 40);
		Drops.put(22672, 40);
		Drops.put(22673, 25);
		Drops.put(22674, 25);
		Drops.put(22675, 25);
		Drops.put(22676, 25);
		Drops.put(22677, 25);
		addKillId(Drops.keys());
		questItemIds = new int[]{ANCIENT_PARCHMENT};
	}

	public static void main(String[] args)
	{
		new _00376_GiantsExploration1();
	}

	@Override
	public int getQuestId()
	{
		return 376;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("31147-02.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("31147-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(QuestType.REPEATABLE);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		else if(NumberUtils.isDigits(event))
		{
			int id = Integer.parseInt(event);
			if(id == 9967) // Recipe - Dynasty Sword (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9968) // Recipe - Dynasty Blade (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9969) // Recipe - Dynasty Phantom (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9970) // Recipe - Dynasty Bow (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9971) // Recipe - Dynasty Knife (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9972) // Recipe - Dynasty Halberd (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9973) // Recipe - Dynasty Cudgel (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9974) // Recipe - Dynasty Mace (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9975) // Recipe - Dynasty Bagh-Nakh (60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 10545) // Recipe - Dynasty Crusher(60%)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if(id == 9628) // Leonard
			{
				htmltext = onExchangeRequest(event, st, 6, 1);
			}
			else if(id == 9629) // Adamantine
			{
				htmltext = onExchangeRequest(event, st, 3, 1);
			}
			else if(id == 9630) // Orichalcum
			{
				htmltext = onExchangeRequest(event, st, 4, 1);
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		if(st.getCond() == 1)
		{
			st.dropQuestItems(ANCIENT_PARCHMENT, -1, -1, Drops.get(npc.getNpcId()), true);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}
		if(st.isStarted())
		{
			htmltext = st.hasQuestItems(BOOK1) && st.hasQuestItems(BOOK2) && st.hasQuestItems(BOOK3) && st.hasQuestItems(BOOK4) && st.hasQuestItems(BOOK5) ? "31147-03.htm" : "31147-02a.htm";
		}
		else
		{
			htmltext = player.getLevel() >= 79 ? "31147-01.htm" : "31147-00.htm";
		}
		return htmltext;
	}

	private String onExchangeRequest(String event, QuestState st, long qty, long rem)
	{
		if(st.getQuestItemsCount(BOOK1) >= rem && st.getQuestItemsCount(BOOK2) >= rem && st.getQuestItemsCount(BOOK3) >= rem && st.getQuestItemsCount(BOOK4) >= rem && st.getQuestItemsCount(BOOK5) >= rem)
		{
			st.takeItems(BOOK1, rem);
			st.takeItems(BOOK2, rem);
			st.takeItems(BOOK3, rem);
			st.takeItems(BOOK4, rem);
			st.takeItems(BOOK5, rem);
			st.giveItems(Integer.parseInt(event), qty);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			return "31147-ok.htm";
		}
		else
		{
			return "31147-no.htm";
		}
	}
}
