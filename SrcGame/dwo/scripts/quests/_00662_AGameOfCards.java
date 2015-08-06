package dwo.scripts.quests;

/**
 * @author ANZO
 * 07.04.2010
 */

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastMap;

public class _00662_AGameOfCards extends Quest
{
	// НПЦшки
	private static final int KLUMP = 30845;

	// Мобы
	private static final int[] mobs = {
		20677, 21109, 21112, 21116, 21114, 21004, 21002, 21006, 21008, 21010, 18001, 20672, 20673, 20674, 20955, 20962,
		20961, 20959, 20958, 20966, 20965, 20968, 20973, 20972, 21278, 21279, 21280, 21281, 21286, 21287, 21288, 21289,
		21520, 21526, 21530, 21535, 21508, 21510, 21513, 21515
	};

	// Квестовые предметы
	private static final short RED_GEM = 8765;
	// Награды
	private static final short Enchant_Weapon_S = 959;
	private static final short Enchant_Weapon_A = 729;
	private static final short Enchant_Weapon_B = 947;
	private static final short Enchant_Weapon_C = 951;
	private static final short Enchant_Weapon_D = 955;
	private static final short Enchant_Armor_D = 956;
	private static final short ZIGGOS_GEMSTONE = 8868;
	// Шанс
	private static final int drop_chance = 35;

	private static final TIntObjectHashMap<CardGame> Games = new TIntObjectHashMap<>();

	public _00662_AGameOfCards()
	{
		addStartNpc(KLUMP);
		addKillId(mobs);
		questItemIds = new int[]{RED_GEM};
	}

	public static void main(String[] args)
	{
		new _00662_AGameOfCards();
	}

	@Override
	public int getQuestId()
	{
		return 662;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equalsIgnoreCase("30845_02.htm") && st.isCreated())
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30845_07.htm") && st.isStarted())
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		else if(event.equalsIgnoreCase("30845_03.htm") && st.isStarted() && st.getQuestItemsCount(RED_GEM) >= 50)
		{
			return "30845_04.htm";
		}
		else if(event.equalsIgnoreCase("30845_10.htm") && st.isStarted())
		{
			if(st.getQuestItemsCount(RED_GEM) < 50)
			{
				return "30845_10a.htm";
			}
			st.takeItems(RED_GEM, 50);
			int player_id = st.getPlayer().getObjectId();
			if(Games.containsKey(player_id))
			{
				Games.remove(player_id);
			}
			Games.put(player_id, new CardGame(player_id));
		}
		else if(event.equalsIgnoreCase("play") && st.isStarted())
		{
			int player_id = st.getPlayer().getObjectId();
			if(!Games.containsKey(player_id))
			{
				return null;
			}
			return Games.get(player_id).playField();
		}
		else if(event.startsWith("card") && st.isStarted())
		{
			int player_id = st.getPlayer().getObjectId();
			if(!Games.containsKey(player_id))
			{
				return null;
			}
			try
			{
				int cardn = Integer.parseInt(event.replaceAll("card", ""));
				return Games.get(player_id).next(cardn, st);
			}
			catch(Exception E)
			{
				return null;
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			if(Rnd.getChance(drop_chance))
			{
				qs.giveItems(RED_GEM, 1);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() != KLUMP)
		{
			return getNoQuestMsg(st.getPlayer());
		}

		if(st.isCreated())
		{
			if(st.getPlayer().getLevel() < 61)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "30845_00.htm";
			}
			st.setCond(0);
			return "30845_01.htm";
		}
		if(st.isStarted())
		{
			return st.getQuestItemsCount(RED_GEM) < 50 ? "30845_03.htm" : "30845_04.htm";
		}

		return getNoQuestMsg(st.getPlayer());
	}

	private static class CardGame
	{
		private static final String[] card_chars = {
			"A", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"
		};
		private static final String html_header = "<html><body>";
		private static final String html_footer = "</body></html>";
		private static final String table_header = "<table border=\"1\" cellpadding=\"3\"><tr>";
		private static final String table_footer = "</tr></table><br><br>";
		private static final String td_begin = "<center><td width=\"50\" align=\"center\"><br><br><br> ";
		private static final String td_end = " <br><br><br><br></td></center>";
		private final String[] cards = new String[5];
		private final int player_id;

		public CardGame(int _player_id)
		{
			player_id = _player_id;
			for(int i = 0; i < cards.length; i++)
			{
				cards[i] = "<a action=\"bypass -h Quest 662_AGameOfCards card" + i + "\">?</a>";
			}
		}

		public String next(int cardn, QuestState st)
		{
			if(cardn >= cards.length || !cards[cardn].startsWith("<a"))
			{
				return null;
			}
			cards[cardn] = card_chars[Rnd.get(card_chars.length)];
			for(String card : cards)
			{
				if(card.startsWith("<a"))
				{
					return playField();
				}
			}
			return finish(st);
		}

		private String finish(QuestState st)
		{
			String result = html_header + table_header;
			FastMap<String, Integer> matches = new FastMap<>();
			for(String card : cards)
			{
				int count = matches.containsKey(card) ? matches.remove(card) : 0;
				count++;
				matches.put(card, count);
			}
			for(String card : cards)
			{
				if(matches.get(card) < 2)
				{
					matches.remove(card);
				}
			}
			String[] smatches = matches.keySet().toArray(new String[matches.size()]);
			Integer[] cmatches = matches.values().toArray(new Integer[matches.size()]);
			String txt = "Хм... Что, вообще ничего? Ну, на этот раз не повезло! Хотите попробовать еще раз?";
			if(cmatches.length == 1)
			{
				if(cmatches[0] == 5)
				{
					txt = "Хм... Это... пятерка! Какая удача! Вам явно помогает богиня победы! Вот Ваш приз! Хорошо сыграл, хорошо заработал!";
					st.giveItems(ZIGGOS_GEMSTONE, 1);
					st.giveItems(Enchant_Weapon_S, 3);
					st.giveItems(Enchant_Weapon_A, 1);
				}
				else if(cmatches[0] == 4)
				{
					txt = "Хм... Это две пары? Неплохо. Вот Ваш приз.";
					st.giveItems(Enchant_Weapon_S, 2);
					st.giveItems(Enchant_Weapon_C, 2);
				}
				else if(cmatches[0] == 3)
				{
					txt = "Хм... Это тройка? Вы счастливчик, скажу я Вам! Вот Ваш приз.";
					st.giveItems(Enchant_Weapon_C, 2);
				}
				else if(cmatches[0] == 2)
				{
					txt = "Хм... Это пара? Сейчас Вам повезло, но посмотрим, что будет дальше. Вот Ваш приз.";
					st.giveItems(Enchant_Armor_D, 2);
				}
			}
			else if(cmatches.length == 2)
			{
				if(cmatches[0] == 3 || cmatches[1] == 3)
				{
					txt = "Хм?.. Это... Фул хаус? Превосходно! Вы играете лучше, чем я думал. Вот Ваш приз.";
					st.giveItems(Enchant_Weapon_A, 1);
					st.giveItems(Enchant_Weapon_B, 2);
					st.giveItems(Enchant_Weapon_D, 1);
				}
				else
				{
					txt = "Хм... Это пара? Сейчас Вам повезло, но посмотрим, что будет дальше. Вот Ваш приз.";
					st.giveItems(Enchant_Weapon_C, 1);
				}
			}

			for(String card : cards)
			{
				if(smatches.length > 0 && smatches[0].equalsIgnoreCase(card))
				{
					result += td_begin + "<font color=\"55FD44\">" + card + "</font>" + td_end;
				}
				else
				{
					result += smatches.length == 2 && smatches[1].equalsIgnoreCase(card) ? td_begin + "<font color=\"FE6666\">" + card + "</font>" + td_end : td_begin + card + td_end;
				}
			}

			result += table_footer + txt;
			if(st.getQuestItemsCount(RED_GEM) >= 50)
			{
				result += "<br><br><a action=\"bypass -h Quest 662_AGameOfCards 30845_10.htm\">Играть еще!</a>";
			}
			result += html_footer;
			Games.remove(player_id);
			return result;
		}

		public String playField()
		{
			StringBuilder result = new StringBuilder(html_header + table_header);
			for(String card : cards)
			{
				result.append(td_begin).append(card).append(td_end);
			}
			result.append(table_footer).append("Ваша следующая карта.").append(html_footer);
			return result.toString();
		}
	}
}
