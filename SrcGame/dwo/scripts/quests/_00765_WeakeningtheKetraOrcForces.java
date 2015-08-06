package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.06.13
 * Time: 13:41
 */

public class _00765_WeakeningtheKetraOrcForces extends Quest
{
	// Квестовые персонажи
	private static final int RUGONESS = 33852;

	// Квестовые предметы
	private static final int SOLDAT_SIGN = 36676;
	private static final int GENERAL_SIGN = 36677;

	// Квестовые монстры
	private static final Map<Integer, Integer> MOBS = new HashMap<>();

	static
	{
		MOBS.put(21324, 500); // Ketra Orc Footman
		MOBS.put(21327, 510); // Ketra Orc Raider
		MOBS.put(21328, 522); // Ketra Orc Scout
		MOBS.put(21329, 519); // Ketra Orc Shaman
		MOBS.put(21331, 529); // Ketra Orc Warrior
		MOBS.put(21332, 529); // Ketra Orc Lieutenant
		MOBS.put(21334, 539); // Ketra Orc Medium
		MOBS.put(21336, 548); // Ketra Orc White Captain
		MOBS.put(21338, 558); // Ketra Orc Seer
		MOBS.put(21339, 568); // Ketra Orc General
		MOBS.put(21340, 568); // Ketra Orc Battalion Commander
		MOBS.put(21342, 578); // Ketra Orc Grand Seer
		MOBS.put(21343, 664); // Ketra Commander
		MOBS.put(21345, 713); // Ketra's Head Shaman
		MOBS.put(21347, 738); // Ketra Prophet
	}

	// Квестовая награда
	private static final int BOX = 37393;

	private static final int[] CLASS_LIMITS = {
		10, 11, 12, 13, 14, 15, 16, 17, 25, 26, 27, 28, 29, 30, 38, 39, 40, 41, 42, 43, 49, 50, 51, 52, 94, 95, 96, 97,
		98, 103, 104, 105, 110, 111, 112, 115, 116, 143, 145, 146
	};

	public _00765_WeakeningtheKetraOrcForces()
	{
		addStartNpc(RUGONESS);
		addTalkId(RUGONESS);
		MOBS.keySet().forEach(this::addKillId);
		questItemIds = new int[]{SOLDAT_SIGN, GENERAL_SIGN};
	}

	public static void main(String[] args)
	{
		new _00765_WeakeningtheKetraOrcForces();
	}

	/***
	 * Расчет награды в зависимости от количества марок генерала
	 * @param st состояние квеста
	 */
	private void calculateReward(QuestState st)
	{
		int boxCount = 0;
		int exp = 0;
		int sp = 0;

		long itemCount = st.getQuestItemsCount(GENERAL_SIGN);

		if(itemCount < 100)
		{
			exp = 19164600;
			sp = 191646;
			boxCount = 1;
		}
		else if(itemCount >= 100 && itemCount < 200)
		{
			exp = 38329200;
			sp = 383292;
			boxCount = 2;
		}
		else if(itemCount >= 200 && itemCount < 300)
		{
			exp = 57493800;
			sp = 574938;
			boxCount = 3;
		}
		else if(itemCount >= 300 && itemCount < 400)
		{
			exp = 76658400;
			sp = 766584;
			boxCount = 4;
		}
		else if(itemCount >= 400 && itemCount < 500)
		{
			exp = 95823000;
			sp = 958230;
			boxCount = 5;
		}
		else if(itemCount >= 500 && itemCount < 600)
		{
			exp = 114987600;
			sp = 1149876;
			boxCount = 6;
		}
		else if(itemCount >= 600 && itemCount < 700)
		{
			exp = 134152200;
			sp = 1341522;
			boxCount = 7;
		}
		else if(itemCount >= 700 && itemCount < 800)
		{
			exp = 153316800;
			sp = 1533168;
			boxCount = 8;
		}
		else if(itemCount >= 800 && itemCount < 900)
		{
			exp = 172481400;
			sp = 1724814;
			boxCount = 9;
		}
		else if(itemCount == 900)
		{
			exp = 191646000;
			sp = 1916460;
			boxCount = 10;
		}
		st.addExpAndSp(exp, sp);
		st.takeItems(SOLDAT_SIGN, -1);
		st.takeItems(GENERAL_SIGN, -1);
		st.giveItems(BOX, boxCount);
	}

	@Override
	public int getQuestId()
	{
		return 765;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "rugoness_q0764_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == RUGONESS)
		{
			switch(reply)
			{
				case 1:
					return "rugoness_q0764_04.htm";
				case 2:
					return "rugoness_q0764_05.htm";
				case 10:
					return "rugoness_q0764_10.htm";
				case 11:
					calculateReward(st);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "rugoness_q0764_11.htm";
				case 13:
					return "rugoness_q0764_13.htm";
				case 20:
					return "rugoness_q0764_14.htm";
				case 21:
					calculateReward(st);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "rugoness_q0764_15.htm";
				case 31:
					calculateReward(st);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "rugoness_q0764_18.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());
		if(st != null && st.isStarted() && Rnd.get(1000) < MOBS.get(npc.getNpcId()))
		{
			if(st.getCond() == 1)
			{
				st.giveItem(SOLDAT_SIGN);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(SOLDAT_SIGN) == 50)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(st.getCond() == 2)
			{
				st.giveItem(GENERAL_SIGN);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(GENERAL_SIGN) == 900)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == RUGONESS)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 76 && player.getLevel() <= 80 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId()) ? "rugoness_q0764_01.htm" : "rugoness_q0764_02.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "rugoness_q0764_07.htm";
						case 2:
							return st.hasQuestItems(GENERAL_SIGN) ? "rugoness_q0764_09.htm" : "rugoness_q0764_08.htm";
						case 3:
							return "rugoness_q0764_17.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 && player.getLevel() <= 80 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId());

	}
}