package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.08.12
 * Time: 8:49
 */

public class _00298_LizardmenConspiracy extends Quest
{
	// Квестовые персонажи
	public static final int PRAGA = 30333;
	public static final int ROHMER = 30344;

	// Квестовые монстры
	public static final int MAILLE_LIZARDMAN_WARRIOR = 20922;
	public static final int MAILLE_LIZARDMAN_SHAMAN = 20923;
	public static final int MAILLE_LIZARDMAN_MATRIARCH = 20924;
	public static final int POISON_ARANEID = 20926;
	public static final int KING_OF_THE_ARANEID = 20927;

	// Квестовые предметы
	public static final int REPORT = 7182;
	public static final int SHINING_GEM = 7183;
	public static final int SHINING_RED_GEM = 7184;

	// Таблица дропа {MOB_ID, ITEM_ID}
	public final int[][] MobsTable = {
		{
			MAILLE_LIZARDMAN_WARRIOR, SHINING_GEM
		}, {
		MAILLE_LIZARDMAN_SHAMAN, SHINING_GEM
	}, {
		MAILLE_LIZARDMAN_MATRIARCH, SHINING_GEM
	}, {
		POISON_ARANEID, SHINING_RED_GEM
	}, {
		KING_OF_THE_ARANEID, SHINING_RED_GEM
	}
	};

	public _00298_LizardmenConspiracy()
	{
		addStartNpc(PRAGA);
		addTalkId(PRAGA, ROHMER);
		for(int[] element : MobsTable)
		{
			addKillId(element[0]);
		}
		questItemIds = new int[]{REPORT, SHINING_GEM, SHINING_RED_GEM};
	}

	public static void main(String[] args)
	{
		new _00298_LizardmenConspiracy();
	}

	@Override
	public int getQuestId()
	{
		return 298;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			qs.giveItems(REPORT, 1);
			return "guard_praga_q0298_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == ROHMER)
		{
			if(reply == 1 && cond == 1)
			{
				st.setCond(2);
				return "magister_rohmer_q0298_0201.htm";
			}
			else if(reply == 3 && cond == 3)
			{
				st.addExpAndSp(0, 42000);
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "magister_rohmer_q0298_0301.htm";
			}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int rand = Rnd.get(10);
		if(st.getCond() == 2)
		{
			for(int[] element : MobsTable)
			{
				if(npcId == element[0])
				{
					if(rand < 6 && st.getQuestItemsCount(element[1]) < 50)
					{
						if(rand < 2 && element[1] == SHINING_GEM)
						{
							st.giveItems(element[1], 2);
						}
						else
						{
							st.giveItems(element[1], 1);
						}
						if(st.getQuestItemsCount(SHINING_GEM) + st.getQuestItemsCount(SHINING_RED_GEM) > 99)
						{
							st.setCond(3);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						else
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == PRAGA)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() < 25)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "guard_praga_q0298_0102.htm";
					}
					else
					{
						return "guard_praga_q0298_0101.htm";
					}
				case STARTED:
					return "guard_praga_q0298_0105.htm";
			}
		}
		else if(npcId == ROHMER)
		{
			if(st.isStarted())
			{
				if(cond < 1)
				{
					return "magister_rohmer_q0298_0202.htm";
				}
				else if(cond == 1)
				{
					st.takeItems(REPORT, -1);
					return "magister_rohmer_q0298_0101.htm";
				}
				else if(cond == 2 || st.getQuestItemsCount(SHINING_GEM) + st.getQuestItemsCount(SHINING_RED_GEM) < 100)
				{
					st.setCond(2);
					return "magister_rohmer_q0298_0204.htm";
				}
				else if(cond == 3 && st.getQuestItemsCount(SHINING_GEM) + st.getQuestItemsCount(SHINING_RED_GEM) >= 100)
				{
					return "magister_rohmer_q0298_0203.htm";
				}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 25;
	}
}