package dwo.scripts.quests;

/**
 * @author ANZO
 */

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00631_DeliciousTopChoiceMeat extends Quest
{
	// Квестовые персонажи
	private static final int TUNATUN = 31537;

	// Квестовые мобы
	private static final int[] MOBS = {
		18874, 18875, 18876, 18877, 18878, 18879, 18881, 18882, 18883, 18884, 18885, 18886, 18888, 18889, 18890, 18891,
		18892, 18893, 18895, 18896, 18897, 18898, 18899, 18900
	};
	private static final int[] RECIPES = {10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381};
	private static final int[] PIECES = {10397, 10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405};
	private static final int[] SHLAK = {15482, 15483};

	// Квестовые предметы
	private static final int PRIME_MEAT = 15534;

	public _00631_DeliciousTopChoiceMeat()
	{
		addStartNpc(TUNATUN);
		addTalkId(TUNATUN);
		addKillId(MOBS);
		questItemIds = new int[]{PRIME_MEAT};
	}

	public static void main(String[] args)
	{
		new _00631_DeliciousTopChoiceMeat();
	}

	@Override
	public int getQuestId()
	{
		return 631;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("31537-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("31537-05.htm") && st.getQuestItemsCount(PRIME_MEAT) >= 120)
		{
			st.setCond(3);
		}
		else if(event.equals("reward_me"))
		{
			event = "31537-07.htm";
			if(st.getQuestItemsCount(PRIME_MEAT) >= 120 && st.getCond() == 3)
			{
				int chance = Rnd.get(100);
				if(chance >= 70)
				{
					st.takeItems(PRIME_MEAT, 120);
					st.giveItems(RECIPES[st.getRandom(RECIPES.length)], 1);
				}
				else if(chance < 70 && chance > 30)
				{
					st.takeItems(PRIME_MEAT, 120);
					st.giveItems(PIECES[st.getRandom(PIECES.length)], Rnd.get(3, 9));
				}
				else
				{
					st.takeItems(PRIME_MEAT, 120);
					st.giveItems(SHLAK[st.getRandom(SHLAK.length)], Rnd.get(1, 2));
				}
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st;
		L2Party party;
		party = player.getParty();
		if(party != null)
		{
			for(L2PcInstance partyMember : party.getMembersInRadius(player, 900))
			{
				st = partyMember.getQuestState(getClass());
				if(st != null && st.getState() == STARTED)
				{
					if(Rnd.getChance(50))
					{
						st.giveItems(PRIME_MEAT, (long) Config.RATE_QUEST_REWARD);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						if(st.getQuestItemsCount(PRIME_MEAT) >= 120)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
			}
		}
		else
		{
			st = player.getQuestState(getClass());
			if(st != null && st.getState() == STARTED)
			{
				if(Rnd.getChance(50))
				{
					st.giveItems(PRIME_MEAT, (long) Config.RATE_QUEST_REWARD);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(PRIME_MEAT) >= 120)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();

		if(st.getState() == CREATED)
		{
			if(player.getLevel() >= 82)
			{
				return "31537-01.htm";
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.REPEATABLE);
				return "31537-02.htm";
			}
		}
		if(cond == 1)
		{
			return "31537-01a.htm";
		}
		if(cond == 2 && st.getQuestItemsCount(PRIME_MEAT) >= 120)
		{
			return "31537-04.htm";
		}
		if(cond == 3)
		{
			return "31537-05.htm";
		}
		return null;
	}
}