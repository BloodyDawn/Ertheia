package dwo.scripts.events;

import dwo.config.events.ConfigEvents;
import dwo.gameserver.datatables.xml.DynamicSpawnData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.npc.drop.EventDropData;
import dwo.gameserver.model.world.npc.drop.EventDropDataTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 22.03.12
 * Time: 10:11
 */

public class AlfabetCollector extends Quest
{
	// Ивентовые персонажи
	private static final int КотАнгел = 4313;

	// Квестовые предметы
	private static final int _A = 3875;
	private static final int _C = 3876;
	private static final int _E = 3877;
	private static final int _F = 3878;
	private static final int _G = 3879;
	private static final int _H = 3880;
	private static final int _I = 3881;
	private static final int _L = 3882;
	private static final int _N = 3883;
	private static final int _O = 3884;
	private static final int _R = 3885;
	private static final int _S = 3886;
	private static final int _T = 3887;
	private static final int _II = 3888;
	private static final int _Y = 13417;
	private static final int _5 = 13418;
	private static final int _M = 22894;
	// Дроп
	private static final int[] _ВсеБуквы = {_A, _C, _E, _F, _G, _H, _I, _L, _N, _O, _R, _S, _T, _II, _Y, _5, _M};
	private static final int ПодарокКоллекционера = 34949;
	// Шансы
	private static final double Шанс = 58823.53;
	private static final int[][] LINEAGEII = {
		{17, 6658, 1},  // Кольцо Баюма
		{517, 34777, 1}, // Свиток Благословения - Ранг R99	Ивент
		{1517, 34776, 1}, // Свиток Благословения - Ранг R95	Ивент
		{3217, 34774, 1}, // Свиток Благословения - Ранг R Ивент
		{6017, 19446, 1}, // Свиток Снятия Оков - Ранг R99
		{6277, 19443, 1},  // Свиток Снятия Оков - Ранг R95
		{6537, 19444, 1},  // Свиток Снятия Оков - Ранг R
		{6797, 18559, 1},  // Синий Кристалл Души - Ранг R99
		{6997, 18558, 1},  // Зеленый Кристалл Души - Ранг R99
		{7197, 18557, 1},  // Красный Кристалл Души - Ранг R99
		{7297, 18556, 1},  // Синий Кристалл Души - Ранг R95
		{7397, 18555, 1},  // Зеленый Кристалл Души - Ранг R95
		{7497, 18554, 1},  // Красный Кристалл Души - Ранг R95
		{7597, 18553, 1},  // Синий Кристалл Души - Ранг R
		{7697, 18552, 1},  // Зеленый Кристалл Души - Ранг R
		{7797, 18551, 1},  // Красный Кристалл Души - Ранг R
		{7897, 34940, 1},  // Свиток: Модифицировать Головной Убор	Ивент
		{7997, 34941, 1},  // Камень Жизни для Головных Уборов	Ивент
		{8097, 19440, 1},  // Самоцвет: Ранг R
		{8197, 2134, 1},  // Самоцвет: Ранг S
		{8297, 2133, 1},  // Самоцвет: Ранг A
		{10297, 13401, 1},  // Свитки Телепорта
		{11297, 13402, 1}, {12297, 13408, 1}, {13297, 13404, 1}, {14297, 13407, 1}, {15297, 13406, 1},
		{16297, 13410, 1}, {17297, 13411, 1}, {18297, 13412, 1}, {19297, 13413, 1}, {100000, 14701, 1}
		// Великое Быстродействующее Зелье Исцеления
	};

	// TODO: http://godworld.ru/scrupload/i/5b4902.png
	private static final int[][] NCSOFT = {
		{25, 6661, 1}, {575, 8752, 1}, {1475, 8742, 1}, {3475, 3959, 1}, {6475, 3958, 1}, {6975, 13429, 1},
		{7475, 13430, 1}, {7975, 13431, 1}, {11975, 13425, 1}, {16975, 13426, 1}, {100000, 14701, 1}
		// Великое Быстродействующее Зелье Исцеления
	};

	// TODO: http://godworld.ru/scrupload/i/338519.png
	private static final int[][] HARMONY = {
		{50, 6660, 1}, {1050, 8762, 1}, {3050, 8752, 2}, {6050, 3959, 2}, {10050, 3958, 2}, {10550, 13429, 1},
		{11050, 13430, 1}, {11550, 13431, 1}, {21550, 13422, 1}, {33550, 13423, 1}, {100000, 14701, 1}
		// Великое Быстродействующее Зелье Исцеления
	};

	private static final String SPAWN_HOLDER = "AlfabetCollector";

	public AlfabetCollector()
	{
		addStartNpc(КотАнгел);
		addTalkId(КотАнгел);

		EventDropData dropData;
		for(int drop : _ВсеБуквы)
		{
			dropData = new EventDropData(drop, 1, 1, ConfigEvents.ALFABET_MOB_MIN_LEVEL, ConfigEvents.ALFABET_MOB_MAX_LEVEL, Шанс);
			EventDropDataTable.getInstance().addEventDrop(dropData);
		}

		SpawnsHolder holder = DynamicSpawnData.getInstance().getSpawnsHolder(SPAWN_HOLDER);
		if(holder == null)
		{
			_log.log(Level.WARN, "Spawn holder [" + SPAWN_HOLDER + "] for class: " + getClass().getSimpleName() + " is null!");
			return;
		}
		holder.spawnAll();
	}

	public static void main(String[] args)
	{
		if(ConfigEvents.EVENT_ALFABET_ENABLE)
		{
			_log.log(Level.INFO, "[EVENTS] : Alfabet Collector Event Enabled");
			new AlfabetCollector();
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.contains("htm"))
		{
			return event;
		}
		QuestState st = player.getQuestState("AlfabetCollector");
		if(checkWord(st, event))
		{
			int rnd = Rnd.get(100000);
			for(int[] reward : getRewards(event))
			{
				if(rnd < reward[0])
				{
					st.giveItems(reward[1], reward[2]);
					break;
				}
			}
			st.giveItem(ПодарокКоллекционера);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			return null; // TODO: Диалог после награды если он есть конечно
		}
		else
		{
			return "4313-noletters.htm";
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState("AlfabetCollector");
		if(st == null)
		{
			st = newQuestState(player);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return "4313.htm";
	}

	/**
	 * @param word слово
	 * @return массив наград для указанного слова
	 */
	private int[][] getRewards(String word)
	{
		if(word.equals("harmony"))
		{
			return HARMONY;
		}
		if(word.equals("ncsoft"))
		{
			return NCSOFT;
		}
		if(word.equals("lineageii"))
		{
			return LINEAGEII;
		}
		return new int[][]{};
	}

	/**
	 * Проверяет наличие собранного слова и если букв хватает - удаляет их
	 *
	 * @param st состояние квеста игрока
	 * @param word проверяемое слово
	 * @return {@code true} если хватает букв на слово
	 */
	private boolean checkWord(QuestState st, String word)
	{
		switch(word)
		{
			case "lineageii":
				if(st.hasQuestItems(_L) && st.hasQuestItems(_I) && st.hasQuestItems(_N) && st.getQuestItemsCount(_E) >= 2 && st.hasQuestItems(_A) && st.hasQuestItems(_G) && st.hasQuestItems(_II))
				{
					st.takeItems(_L, 1);
					st.takeItems(_I, 1);
					st.takeItems(_N, 1);
					st.takeItems(_E, 2);
					st.takeItems(_A, 1);
					st.takeItems(_G, 1);
					st.takeItems(_II, 1);
					return true;
				}
				break;
			case "ncsoft":
				if(st.hasQuestItems(_N) && st.hasQuestItems(_C) && st.hasQuestItems(_S) && st.hasQuestItems(_O) && st.hasQuestItems(_F) && st.hasQuestItems(_T))
				{
					st.takeItems(_N, 1);
					st.takeItems(_C, 1);
					st.takeItems(_S, 1);
					st.takeItems(_O, 1);
					st.takeItems(_F, 1);
					st.takeItems(_T, 1);
					return true;
				}
				break;
			case "harmony":
				if(st.hasQuestItems(_H) && st.hasQuestItems(_A) && st.hasQuestItems(_R) && st.hasQuestItems(_M) && st.hasQuestItems(_O) && st.hasQuestItems(_N) && st.hasQuestItems(_Y))
				{
					st.takeItems(_H, 1);
					st.takeItems(_A, 1);
					st.takeItems(_R, 1);
					st.takeItems(_M, 1);
					st.takeItems(_O, 1);
					st.takeItems(_N, 1);
					st.takeItems(_Y, 1);
					return true;
				}
				break;
		}
		return false;
	}
}
