package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.06.13
 * Time: 15:40
 */

public class _00759_TheCeaselessNightmareofDwarves extends Quest
{
	// Квестовые персонажи
	private static final int DAICHIR = 30537;

	// Квестовые монстры
	private static final int TRASKEN = 29197;

	public _00759_TheCeaselessNightmareofDwarves()
	{
		addStartNpc(DAICHIR);
		addTalkId(DAICHIR);
		addKillId(TRASKEN);

		questItemIds = new int[]{};
	}

	public static void main(String[] args)
	{
		new _00759_TheCeaselessNightmareofDwarves();
	}

	/***
	 * Расчет награды для игрока
	 * @param st состояние квеста
	 */
	public void calculateReward(QuestState st)
	{
		int rndNum = Rnd.get(1, 18);
		int itemID = 0;
		switch(rndNum)
		{
			case 1:
				itemID = 17623; // Кольцо Земляного Червя
				break;
			case 2:
				itemID = 35389; // Часть Благословенного Острия Хелиоса
				break;
			case 3:
				itemID = 35390; // Часть Благословенного Резака Хелиоса
				break;
			case 4:
				itemID = 35391; // Часть Благословенного Эспадона Хелиоса
				break;
			case 5:
				itemID = 35392; // Часть Благословенного Мстителя Хелиоса
				break;
			case 6:
				itemID = 35393; // Часть Благословенного Воителя Хелиоса
				break;
			case 7:
				itemID = 35394; // Часть Благословенного Буревестника Хелиоса
				break;
			case 8:
				itemID = 35395; // Часть Благословенного Броска Хелиоса
				break;
			case 9:
				itemID = 35396; // Часть Благословенного Стража Хелиоса
				break;
			case 10:
				itemID = 35397; // Часть Благословенного Расчленителя Хелиоса
				break;
			case 11:
				itemID = 35398; // Часть Благословенного Заклинателя Хелиоса
				break;
			case 12:
				itemID = 35399; // Часть Благословенного Возмездия Хелиоса
				break;
			case 13:
				itemID = 9552; // Кристалл Огня
				break;
			case 14:
				itemID = 9553; // Кристалл Воды
				break;
			case 15:
				itemID = 9554; // Кристалл Земли
				break;
			case 16:
				itemID = 9555; // Кристалл Ветра
				break;
			case 17:
				itemID = 9556; // Кристалл Тьмы
				break;
			case 18:
				itemID = 9557; // Кристалл Святости
				break;
		}
		st.giveItem(itemID);
	}

	@Override
	public int getQuestId()
	{
		return 759;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "daichir_priest_of_earth_q0759_08.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == DAICHIR)
		{
			switch(reply)
			{
				case 1:
					return "daichir_priest_of_earth_q0759_05.htm";
				case 2:
					return "daichir_priest_of_earth_q0759_06.htm";
				case 11:
					return "daichir_priest_of_earth_q0759_10.htm";
				case 12:
					return "daichir_priest_of_earth_q0759_11.htm";
				case 14:
					return "daichir_priest_of_earth_q0759_14.htm";
				case 20:
					if(st.getCond() == 2)
					{
						calculateReward(st);
						st.exitQuest(QuestType.DAILY);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "daichir_priest_of_earth_q0759_16.htm";
					}
					return null;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == TRASKEN)
		{
			executeForEachPlayer(killer, npc, isPet, true, true);
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

		if(npc.getNpcId() == DAICHIR)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "daichir_priest_of_earth_q0759_03.htm";
				case CREATED:
					if(player.getLevel() >= 98)
					{
						return "daichir_priest_of_earth_q0759_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "daichir_priest_of_earth_q0759_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "daichir_priest_of_earth_q0759_09.htm";
					}
					else if(st.getCond() == 2)
					{
						return "daichir_priest_of_earth_q0759_15.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 98;

	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1 && Util.checkIfInRange(1500, npc, player, false))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}
}