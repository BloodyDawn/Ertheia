package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 17:52
 */

public class _00143_FallenAngelRequestofDusk extends Quest
{
	// Квесьлвые перонажи
	private static final int NATOOLS = 30894;
	private static final int TOBIAS = 30297;
	private static final int CASIAN = 30612;
	private static final int ROCK = 32368;
	private static final int ANGEL = 32369;

	// Квестовые предметы
	private static final int SEALED_PATH = 10354;
	private static final int PATH = 10355;
	private static final int EMPTY_CRYSTAL = 10356;
	private static final int MEDICINE = 10357;
	private static final int MESSAGE = 10358;

	private boolean angelSpawned;

	public _00143_FallenAngelRequestofDusk()
	{
		// Нет стартового NPC, квест стартуется с менюшки в 142 квесте
		addTalkId(NATOOLS, TOBIAS, CASIAN, ROCK, ANGEL);
		questItemIds = new int[]{SEALED_PATH, PATH, EMPTY_CRYSTAL, MEDICINE, MESSAGE};
	}

	public static void main(String[] args)
	{
		new _00143_FallenAngelRequestofDusk();
	}

	@Override
	public int getQuestId()
	{
		return 143;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			QuestState pqs = qs.getPlayer().getQuestState(_00142_FallenAngelRequestofDawn.class);
			if(pqs != null)
			{
				pqs.exitQuest(QuestType.REPEATABLE);
			}
			qs.startQuest();
			return "warehouse_chief_natools_q0143_01.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == NATOOLS)
		{
			switch(reply)
			{
				case 4:
					return "warehouse_chief_natools_q0143_03.htm";
				case 5:
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(SEALED_PATH, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "warehouse_chief_natools_q0143_04.htm";
			}
		}
		else if(npcId == TOBIAS)
		{
			switch(reply)
			{
				case 3:
					return "master_tobias_q0143_03.htm";
				case 4:
					return "master_tobias_q0143_04.htm";
				case 5:
					st.setCond(3);
					st.unset("talk");
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(PATH, 1);
					st.giveItems(EMPTY_CRYSTAL, 1);
					return "master_tobias_q0143_05.htm";
			}
		}
		else if(npcId == CASIAN)
		{
			switch(reply)
			{
				case 5:
					return "sage_kasian_q0143_03.htm";
				case 6:
					return "sage_kasian_q0143_04.htm";
				case 7:
					return "sage_kasian_q0143_06.htm";
				case 8:
					return "sage_kasian_q0143_07.htm";
				case 9:
					return "sage_kasian_q0143_08.htm";
				case 10:
					st.setCond(4);
					st.unset("talk");
					st.giveItems(MEDICINE, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sage_kasian_q0143_09.htm";
			}
		}
		else if(npcId == ROCK)
		{
			switch(reply)
			{
				case 1:
					if(angelSpawned)
					{
						return "stained_rock_q0142_04.htm";
					}
					else
					{
						st.addSpawn(ANGEL, -21882, 186730, -4320, 0, false, 180000);
						angelSpawned = true;
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						startQuestTimer("angel_cleanup", 180000, null, player);
						return "stained_rock_q0142_05.htm";
					}
			}
		}
		else if(npcId == ANGEL)
		{
			switch(reply)
			{
				case 1:
					st.takeItems(MEDICINE, -1);
					st.set("talk", "1");
					return "q_fallen_angel_npc_q0143_04.htm";
				case 3:
					return "q_fallen_angel_npc_q0143_05.htm";
				case 4:
					return "q_fallen_angel_npc_q0143_07.htm";
				case 5:
					return "q_fallen_angel_npc_q0143_08.htm";
				case 6:
					return "q_fallen_angel_npc_q0143_09.htm";
				case 7:
					return "q_fallen_angel_npc_q0143_11.htm";
				case 8:
					return "q_fallen_angel_npc_q0143_12.htm";
				case 9:
					return "q_fallen_angel_npc_q0143_13.htm";
				case 10:
					st.setCond(5);
					st.unset("talk");
					st.takeItems(EMPTY_CRYSTAL, -1);
					st.giveItems(MESSAGE, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					npc.getLocationController().delete();
					return "q_fallen_angel_npc_q0143_14.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		// Если уже выбрали кокурирующий квест, то шлем игрока куда подальше
		QuestState pst = st.getPlayer().getQuestState(_00142_FallenAngelRequestofDawn.class);
		if(pst != null && pst.isStarted())
		{
			st.exitQuest(QuestType.REPEATABLE);
			return getNoQuestMsg(st.getPlayer());
		}

		if(npcId == NATOOLS)
		{
			switch(cond)
			{
				case 0:
					return "warehouse_chief_natools_q0143_01.htm";
				case 1:
					return "warehouse_chief_natools_q0143_02.htm";
				case 2:
					return "warehouse_chief_natools_q0143_05.htm";
			}
		}
		else if(npcId == TOBIAS)
		{
			if(cond == 2)
			{
				if(st.getInt("talk") == 1)
				{
					return "master_tobias_q0143_03.htm";
				}
				else
				{
					st.takeItems(SEALED_PATH, -1);
					st.set("talk", "1");
					return "master_tobias_q0143_02.htm";
				}
			}
			else if(cond == 3)
			{
				return "master_tobias_q0143_06.htm";
			}
			else if(cond == 5)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.giveAdena(89046, true);
				st.exitQuest(QuestType.REPEATABLE);
				return "master_tobias_q0143_07.htm";
			}
		}
		else if(npcId == CASIAN)
		{
			if(cond == 3)
			{
				if(st.getInt("talk") == 1)
				{
					return "sage_kasian_q0143_03.htm";
				}
				else
				{
					st.takeItems(PATH, -1);
					st.set("talk", "1");
					return "sage_kasian_q0143_02.htm";
				}
			}
			else if(cond == 4)
			{
				return "sage_kasian_q0143_10.htm";
			}
		}
		else if(npcId == ROCK)
		{
			if(cond <= 3)
			{
				return "stained_rock_q0143_01.htm";
			}
			else
			{
				return cond == 4 ? "stained_rock_q0143_02.htm" : "stained_rock_q0143_06.htm";
			}
		}
		else if(npcId == ANGEL)
		{
			if(cond == 4)
			{
				return st.getInt("talk") == 1 ? "q_fallen_angel_npc_q0143_04.htm" : "q_fallen_angel_npc_q0143_03.htm";
			}
			else if(cond == 5)
			{
				return "q_fallen_angel_npc_q0143_14.htm";
			}
		}
		return null;
	}
}
