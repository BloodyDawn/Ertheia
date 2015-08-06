package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

public class _00663_SeductiveWhispers extends Quest
{
	// НПЦ
	private static final int WILBERT = 30846;
	private static final int[] MOBS = {
		20674, 20678, 20954, 20955, 20956, 20957, 20958, 20959, 20960, 20961, 20962, 20974, 20975, 20976, 20996, 20997,
		20998, 20999, 21001, 21002, 21006, 21007, 21008, 21009, 21010
	};

	// Квест итемы
	private static final int SPIRIT_BEAD = 8766;

	// Шансы дропа
	private static final int DROP_CHANCE = 80;
	private static final int WIN_ROUND_CHANCE = 66;

	// Награды
	private static final int EWA = 729; // Scroll: Enchant Weapon A
	private static final int EAA = 730; // Scroll: Enchant Armor A
	private static final int EWB = 947; // Scroll: Enchant Weapon B
	private static final int EAB = 948; // Scroll: Enchant Armor B
	private static final int EWC = 951; // Scroll: Enchant Weapon C
	private static final int EWD = 955; // Scroll: Enchant Weapon D

	// ====== Rewards -  B grade 60% weapon recipes & keymats =========
	// These are just most popular B weapons, need retail check here
	// Blunts: Art of Battle Axe, Staff of Evil Spirits (2)
	// Bows: Bow of Peril (1)
	// Daggers: Demon Dagger, Kris (2)
	// Fists: Bellion Cestus (1)
	// Polearms: Lance (1)
	// Swords: Great Sword, Keshanberk, Sword of Valhalla (3)
	// ====== Total: 10; In that order they come in a set below: ======
	private static final int[] B_RECIPES = {4963, 4966, 4967, 4968, 5001, 5003, 5004, 5005, 5006, 5007};
	private static final int[] B_KEYMATS = {4101, 4107, 4108, 4109, 4115, 4117, 4118, 4119, 4120, 4121};

	public _00663_SeductiveWhispers()
	{

		addStartNpc(WILBERT);
		addTalkId(WILBERT);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00663_SeductiveWhispers();
	}

	@Override
	public int getQuestId()
	{
		return 663;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("Wilbert_IWantToPlay.htm"))
		{
			st.startQuest();
			st.set("round", "0");
		}
		else if(event.equalsIgnoreCase("Wilbert_ExitQuest.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		else if(event.equalsIgnoreCase("Wilbert_IWantToPractice.htm"))
		{
			int beads = (int) st.getQuestItemsCount(SPIRIT_BEAD);
			if(beads < 1)
			{
				event = "Wilbert_Practice_NotEnoughBeads.htm";
			}
		}
		else if(event.equalsIgnoreCase("Wilbert_Practice.htm"))
		{
			int beads = (int) st.getQuestItemsCount(SPIRIT_BEAD); // получаем количество бусин, должно быть > 1
			if(beads < 1)
			{
				event = "Wilbert_Practice_NotEnoughBeads.htm";
			}
			else
			{
				st.takeItems(SPIRIT_BEAD, 1); // забираем одну бусину для теста на удачу
				int random = st.getRandom(100);
				event = random < WIN_ROUND_CHANCE ? "Wilbert_PracticeWon.htm" : "Wilbert_PracticeLost.htm";
			}
		}
		else if(event.equalsIgnoreCase("Wilbert_LetsPlay.htm"))
		{
			int beads = (int) st.getQuestItemsCount(SPIRIT_BEAD);
			if(beads < 50)
			{
				event = "Wilbert_Practice_NotEnoughBeads.htm";
			}
			else
			{
				event = "Wilbert_PlayRound1.htm";
				st.set("round", "0");
			}
		}
		else if(event.equalsIgnoreCase("Wilbert_PullCard.htm"))
		{
			int round = st.getInt("round");
			int beads = (int) st.getQuestItemsCount(SPIRIT_BEAD);
			if(beads < 50 && round == 0)
			{
				event = "Wilbert_Practice_NotEnoughBeads.htm";
			}
			else
			{
				if(round == 0)
				{
					st.takeItems(SPIRIT_BEAD, 50);
				}
				int random = st.getRandom(100);
				if(random > WIN_ROUND_CHANCE)
				{
					event = "Wilbert_PlayLose.htm";
					st.set("round", "0");
				}
				else
				{
					round += 1;
					event = st.showHtmlFile("Wilbert_PlayWin.htm").replace("NROUND", String.valueOf(round));
					switch(round)
					{
						case 1:
							event = event.replace("MYPRIZE", "40,000 Аден");
							break;
						case 2:
							event = event.replace("MYPRIZE", "80,000 Аден");
							break;
						case 3:
							event = event.replace("MYPRIZE", "110,000 Аден, D-grade Enchant Weapon Scroll");
							break;
						case 4:
							event = event.replace("MYPRIZE", "199,000 Аден, C-grade Enchant Weapon Scroll");
							break;
						case 5:
							event = event.replace("MYPRIZE", "388,000 Аден, 1 рецепт для a B-grade оружия");
							break;
						case 6:
							event = event.replace("MYPRIZE", "675,000 Аден, 1 индигриент для B-grade оружия");
							break;
						case 7:
							event = event.replace("MYPRIZE", "1,284,000 Аден, 2 B-grade Enchant Weapon Scrolls, 2 B-grade Enchat Armor Scrolls");
							break;
						case 8:
							round = 0;
							st.giveAdena(2384000, true);
							st.giveItems(EWA, 1); // Scroll: Enchant Weapon A
							st.giveItems(EAA, 2); // Scroll: Enchant Armor A
							event = "Wilbert_PlayWonRound8.htm";
							break;
					}
					st.set("round", String.valueOf(round));
				}
			}
		}
		else if(event.equalsIgnoreCase("Wilbert_TakePrize.htm"))
		{
			int round = st.getInt("round");
			if(round == 0)
			{
				event = "<html><body>Вы не выиграли ни одно раунда! Нет призов.</body></html>";
				return event;
			}
			if(round > 8)
			{
				st.set("round", "0");
				event = "<html><body>Читер цуко кыш кыш кыш :D</body></html>";
				return event;
			}
			st.set("round", "0");
			event = "Wilbert_PrizeTaken.htm";
			switch(round)
			{
				case 1:
					st.giveAdena(40000, true);
					break;
				case 2:
					st.giveAdena(80000, true);
					break;
				case 3:
					st.giveAdena(110000, true);
					st.giveItems(EWD, 1);
					break;
				case 4:
					st.giveAdena(199000, true);
					st.giveItems(EWC, 1);
					break;
				case 5:
					st.giveAdena(388000, true);
					st.giveItems(B_RECIPES[st.getRandom(B_RECIPES.length)], 1);
					break;
				case 6:
					st.giveAdena(675000, true);
					st.giveItems(B_KEYMATS[st.getRandom(B_KEYMATS.length)], 1);
					break;
				case 7:
					st.giveAdena(1284000, true);
					st.giveItems(EWB, 2);
					st.giveItems(EAB, 2);
					break;
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
			return null;
		}

		int npcId = npc.getNpcId();
		if(ArrayUtils.contains(MOBS, npcId))
		{
			if(st.getState() == STARTED)
			{
				st.dropQuestItems(SPIRIT_BEAD, 1, -1, DROP_CHANCE, true);
			}
			else
			{
				return null;
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		int npcId = npc.getNpcId();
		QuestStateType id = st.getState();
		if(npcId == WILBERT && id == CREATED)
		{
			if(player.getLevel() >= 50)
			{
				return "Wilbert_start.htm";
			}
			else
			{

				st.exitQuest(QuestType.REPEATABLE);
				return "<html><body>Этот квест доступен персонажам, достигшим 50 уровня.</body></html>";
			}
		}
		if(npcId == WILBERT && id == STARTED)
		{
			return "Wilbert_QuestInProgress.htm";
		}
		return null;
	}
}
