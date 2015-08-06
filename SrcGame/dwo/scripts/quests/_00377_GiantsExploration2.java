package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.math.NumberUtils;

public class _00377_GiantsExploration2 extends Quest
{
	// NPC
	private static final int SOBLING = 31147;

	// Items
	private static final int TITAN_ANCIENT_BOOK = 14847;
	private static final int BOOK1 = 14842;
	private static final int BOOK2 = 14843;
	private static final int BOOK3 = 14844;
	private static final int BOOK4 = 14845;
	private static final int BOOK5 = 14846;

	private static final TIntObjectHashMap<Integer> Drops = new TIntObjectHashMap<>();

	public _00377_GiantsExploration2()
	{
		addStartNpc(SOBLING);
		addTalkId(SOBLING);

		// Droplist: mobId, chance
		Drops.put(22661, 220);
		Drops.put(22662, 230);
		Drops.put(22663, 230);
		Drops.put(22664, 230);
		Drops.put(22665, 230);
		Drops.put(22666, 25);
		Drops.put(22667, 25);
		Drops.put(22668, 25);
		Drops.put(22669, 25);
		for(int i : Drops.keys())
		{
			addKillId(i);
		}
		questItemIds = new int[]{TITAN_ANCIENT_BOOK};
	}

	public static void main(String[] args)
	{
		new _00377_GiantsExploration2();
	}

	@Override
	public int getQuestId()
	{
		return 377;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
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
		else if(event.equals("rewardBook"))
		{
			if(st.getQuestItemsCount(BOOK1) >= 5 && st.getQuestItemsCount(BOOK2) >= 5 && st.getQuestItemsCount(BOOK3) >= 5 && st.getQuestItemsCount(BOOK4) >= 5 && st.getQuestItemsCount(BOOK5) >= 5)
			{
				st.giveItems(Rnd.getChance(50) ? 9626 : 9625, 1); // Giant's Codex - Oblivion or Giant's Codex - Discipline
				st.takeItems(BOOK1, 5);
				st.takeItems(BOOK2, 5);
				st.takeItems(BOOK3, 5);
				st.takeItems(BOOK4, 5);
				st.takeItems(BOOK5, 5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "31147-ok.htm";
			}
			else
			{
				return "31147-no.htm";
			}
		}
		else if(event.equals("randomReward"))
		{
			if(st.hasQuestItems(BOOK1) && st.hasQuestItems(BOOK2) && st.hasQuestItems(BOOK3) && st.hasQuestItems(BOOK4) && st.hasQuestItems(BOOK5))
			{
				int[][] reward = {{9628, 6}, {9629, 3}, {9630, 4}};
				int rnd = Rnd.get(reward.length);
				st.giveItems(reward[rnd][0], reward[rnd][1]);
				st.takeItems(BOOK1, 1);
				st.takeItems(BOOK2, 1);
				st.takeItems(BOOK3, 1);
				st.takeItems(BOOK4, 1);
				st.takeItems(BOOK5, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "31147-ok.htm";
			}
			else
			{
				return "31147-no.htm";
			}
		}
		else if(NumberUtils.isDigits(event))
		{
			if(st.hasQuestItems(BOOK1) && st.hasQuestItems(BOOK2) && st.hasQuestItems(BOOK3) && st.hasQuestItems(BOOK4) && st.hasQuestItems(BOOK5))
			{
				int itemId = Integer.parseInt(event);
				int count = 1;
				if(itemId == 9628)
				{
					count = 6;
				}
				else if(itemId == 9629)
				{
					count = 3;
				}
				else if(itemId == 9630)
				{
					count = 4;
				}
				st.giveItems(itemId, count);
				st.takeItems(BOOK1, 1);
				st.takeItems(BOOK2, 1);
				st.takeItems(BOOK3, 1);
				st.takeItems(BOOK4, 1);
				st.takeItems(BOOK5, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "31147-ok.htm";
			}
			else
			{
				return "31147-no.htm";
			}
		}
		return event;
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
			st.dropQuestItems(TITAN_ANCIENT_BOOK, -1, -1, Drops.get(npc.getNpcId()), true);
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
}
