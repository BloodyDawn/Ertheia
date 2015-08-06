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
 * Time: 12:05
 */
public class _00764_WeakeningtheVarkaSilenosForces extends Quest
{
	// Квестовые персонажи
	private static final int HANSEN = 33853;

	// Квестовые предметы
	private static final int SOLDAT_SIGN = 36674;
	private static final int GENERAL_SIGN = 36755;

	// Квестовые монстры
	private static final Map<Integer, Integer> MOBS = new HashMap<>();

	static
	{
		MOBS.put(21350, 500); // Varka Silenos Recruit
		MOBS.put(21353, 510); // Varka Silenos Scout
		MOBS.put(21354, 522); // Varka Silenos Hunter
		MOBS.put(21355, 519); // Varka Silenos Shaman
		MOBS.put(21357, 529); // Varka Silenos Priest
		MOBS.put(21358, 529); // Varka Silenos Warrior
		MOBS.put(21360, 539); // Varka Silenos Medium
		MOBS.put(21362, 539); // Varka Silenos Officer
		MOBS.put(21364, 558); // Varka Silenos Seer
		MOBS.put(21365, 568); // Varka Silenos Great Magus
		MOBS.put(21366, 568); // Varka Silenos General
		MOBS.put(21368, 568); // Varka Silenos Great Seer
		MOBS.put(21369, 664); // Varka's Commander
		MOBS.put(21371, 713); // Varka's Head Magus
		MOBS.put(21373, 738); // Varka's Prophet
	}

	// Квестовая награда
	private static final int BOX = 37393;

	private static final int[] CLASS_LIMITS = {
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 19, 20, 21, 22, 23, 24, 31, 32, 33, 34, 35, 36, 37, 44, 45, 46, 47, 48, 53,
		54, 55, 56, 57, 88, 89, 90, 91, 92, 93, 99, 100, 101, 102, 106, 107, 108, 109, 113, 114, 117, 118, 123, 124,
		125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 139, 140, 141, 142, 144
	};

	public _00764_WeakeningtheVarkaSilenosForces()
	{
		addStartNpc(HANSEN);
		addTalkId(HANSEN);
		MOBS.keySet().forEach(this::addKillId);
		questItemIds = new int[]{SOLDAT_SIGN, GENERAL_SIGN};
	}

	public static void main(String[] args)
	{
		new _00764_WeakeningtheVarkaSilenosForces();
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
		return 764;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "hansen_q0764_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == HANSEN)
		{
			switch(reply)
			{
				case 1:
					return "hansen_q0764_04.htm";
				case 2:
					return "hansen_q0764_05.htm";
				case 10:
					return "hansen_q0764_10.htm";
				case 11:
					calculateReward(st);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "hansen_q0764_11.htm";
				case 13:
					return "hansen_q0764_13.htm";
				case 20:
					return "hansen_q0764_14.htm";
				case 21:
					calculateReward(st);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "hansen_q0764_15.htm";
				case 31:
					calculateReward(st);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "hansen_q0764_18.htm";
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

		if(npc.getNpcId() == HANSEN)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 76 && player.getLevel() <= 80 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId()) ? "hansen_q0764_01.htm" : "hansen_q0764_02.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "hansen_q0764_07.htm";
						case 2:
							return st.hasQuestItems(GENERAL_SIGN) ? "hansen_q0764_09.htm" : "hansen_q0764_08.htm";
						case 3:
							return "hansen_q0764_17.htm";
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