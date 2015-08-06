package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.02.12
 * Time: 3:48
 * TODO: Не доделан
 */

public class _10304_ForForgottenHeroes extends Quest
{
	// Квестовые персонажи
	private static final int Изшаэль = 32894;

	// Рейдовые боссы в Фортуне
	private static final int Рыдающая_Юи = 25837;
	private static final int Трусливый_Мукшу = 25838;
	private static final int Слепой_Хорнапи = 25839;
	private static final int Разъяренный_Мастер_Киннен = 25840;
	private static final int Сэр_Тьмы_Ресинда = 25841;
	private static final int Магический_Воин_Коняр = 25843; // Или 25844,25845
	private static final int Йоентумак_Ожидания = 25846;
	private static final int Фрон = 25824; // Или 25825

	// Квестовые предметы
	private static final int Ветхий_Свиток = 34033;
	private static final int Карта_Фортуны = 34034;

	private static final int Мешочек_с_Доспехом_R2 = 33467;
	private static final int Мешочек_с_Оружием_R2 = 33466;
	private static final int Мешочек_с_Усилителем_R = 32779;

	private static final int СвитокМодифицироватьОружие = 17526;
	private static final int СвитокМодифицироватьДоспех = 17527;

	private static final int НаковальняКузнецаГигантов = 19307;
	private static final int ЗаготовкаОружейникаГигантов = 19308;
	private static final int ДревняяРукоятьКузнеца = 19514;
	private static final int ДревняяЗаготовкаРеорина = 19515;
	private static final int ДревняяНаковальняКузнеца = 19516;
	private static final int ДревняяЗаготовкаОружейника = 19517;

	public _10304_ForForgottenHeroes()
	{
		addStartNpc(Изшаэль);
		addTalkId(Изшаэль);
		addKillId(Рыдающая_Юи, Трусливый_Мукшу, Слепой_Хорнапи, Разъяренный_Мастер_Киннен, Сэр_Тьмы_Ресинда, Магический_Воин_Коняр, Йоентумак_Ожидания, Фрон);
		questItemIds = new int[]{Карта_Фортуны};
	}

	public static void main(String[] args)
	{
		new _10304_ForForgottenHeroes();
	}

	@Override
	public int getQuestId()
	{
		return 10304;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(player.getLevel() < 90)
		{
			return getLowLevelMsg(90);
		}

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("32894-01.htm"))
		{
			st.takeItems(Карта_Фортуны, -1);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("enter"))
		{
			return player.isInParty() && player.getParty().getMemberCount() == 7 ? "32894-enter.htm" : "32894-no7.htm";
		}
		else if(event.equalsIgnoreCase("32894-04.htm"))
		{
			st.unset("one");
			st.unset("two");
			st.unset("three");
			st.unset("four");
			st.unset("five");
			st.unset("six");
			st.unset("seven");
			st.unset("eight");
			st.addExpAndSp(15197798, 6502166);
			st.giveAdena(47085998, true);
			st.giveItem(Мешочек_с_Доспехом_R2);
			st.giveItem(Мешочек_с_Оружием_R2);
			st.giveItem(Мешочек_с_Усилителем_R);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Изшаэль)
		{
			if(reply == 1 && cond == 1)
			{
				st.takeItems(Карта_Фортуны, -1);
				st.setCond(2);
				return "izshael_q10304_05.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				return "izshael_q10304_07.htm";
			}
			else if(reply == 3 && cond == 2)
			{
				return player.isInParty() && player.getParty().getMemberCount() == 7 ? "izshael_q10304_09.htm" : "izshael_q10304_08.htm";
			}
			else if(reply == 4 && cond == 9)
			{
				return "izshael_q10304_12.htm";
			}
			else if(reply == 5 && cond == 9)
			{
				st.giveItem(НаковальняКузнецаГигантов);
				st.giveItem(ЗаготовкаОружейникаГигантов);
				st.giveItem(ДревняяРукоятьКузнеца);
				st.giveItem(ДревняяЗаготовкаРеорина);
				st.giveItem(ДревняяНаковальняКузнеца);
				st.giveItem(ДревняяЗаготовкаОружейника);
				st.addExpAndSp(15197798, 6502166);
				st.giveAdena(47085998, true);
				st.exitQuest(QuestType.ONE_TIME);
			}
			else if(reply == 6 && cond == 9)
			{
				st.giveItem(СвитокМодифицироватьДоспех);
				st.giveItem(СвитокМодифицироватьОружие);
				st.addExpAndSp(15197798, 6502166);
				st.giveAdena(47085998, true);
				st.exitQuest(QuestType.ONE_TIME);
			}
			else if(reply == 7 && cond == 9)
			{
				st.giveItems(Мешочек_с_Усилителем_R, 4);
				st.addExpAndSp(15197798, 6502166);
				st.giveAdena(47085998, true);
				st.exitQuest(QuestType.ONE_TIME);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		int cond = st.getCond();

		if(cond >= 2 && cond < 9)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();

			int ONE = st.getInt("_1");
			int TWO = st.getInt("_2");
			int THREE = st.getInt("_3");
			int FOUR = st.getInt("_4");
			int FIVE = st.getInt("_5");
			int SIX = st.getInt("_6");
			int SEVEN = st.getInt("_7");
			int EIGHT = st.getInt("_8");

			if(npc.getNpcId() == Рыдающая_Юи && cond == 2)
			{
				ONE++;
				st.set("one", String.valueOf(ONE));
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(npc.getNpcId() == Разъяренный_Мастер_Киннен && cond == 3)
			{
				TWO++;
				st.set("two", String.valueOf(TWO));
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(npc.getNpcId() == Магический_Воин_Коняр && cond == 4)
			{
				THREE++;
				st.set("three", String.valueOf(THREE));
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(npc.getNpcId() == Сэр_Тьмы_Ресинда && cond == 5)
			{
				FOUR++;
				st.set("four", String.valueOf(FOUR));
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			if(cond == 6)
			{
				if(npc.getNpcId() == Трусливый_Мукшу)
				{
					FIVE++;
					st.set("five", String.valueOf(FIVE));
				}
				else if(npc.getNpcId() == Слепой_Хорнапи)
				{
					SIX++;
					st.set("six", String.valueOf(SIX));
				}
				if(FIVE > 0 && SIX > 0)
				{
					st.setCond(7);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(npc.getNpcId() == Йоентумак_Ожидания && cond == 7)
			{
				SEVEN++;
				st.set("seven", String.valueOf(SEVEN));
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(npc.getNpcId() == Фрон && cond == 8)
			{
				EIGHT++;
				st.set("eight", String.valueOf(EIGHT));
				st.setCond(9);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}

			moblist.put(1025837, ONE);
			moblist.put(1025840, TWO);
			moblist.put(1025845, THREE);
			moblist.put(1025841, FOUR);
			moblist.put(1025838, FIVE);
			moblist.put(1025839, SIX);
			moblist.put(1025846, SEVEN);
			moblist.put(1025825, EIGHT);

			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		L2PcInstance player = st.getPlayer();

		QuestState prevst = player.getQuestState(_10302_TheShadowOfAnxiety.class);

		if(npcId == Изшаэль)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "izshael_q10304_03.htm";
				case STARTED:
					switch(cond)
					{
						case 1:
							if(player.getLevel() >= 90)
							{
								if(prevst != null && prevst.isCompleted())
								{
									return "izshael_q10304_04.htm";
								}
								else
								{
									st.exitQuest(QuestType.REPEATABLE);
									return "izshael_q10304_02.htm";
								}
							}
							else
							{
								st.exitQuest(QuestType.REPEATABLE);
								return "izshael_q10304_01.htm";
							}
						case 2:
							return "izshael_q10304_06.htm";
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
						case 8:
							return "izshael_q10304_07.htm";
						case 9:
							return "izshael_q10304_11.htm";
					}
					break;
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10302_TheShadowOfAnxiety.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}

	@Override
	public String onStartFromItem(L2PcInstance player)
	{
		if(player.getLevel() < 90)
		{
			return "q10304_fortuna_map_err002.htm";
		}
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}
		st.startQuest();
		if(st.getQuestItemsCount(Ветхий_Свиток) > 0)
		{
			st.takeItems(Ветхий_Свиток, -1);
		}
		else
		{
			return "q10304_fortuna_map_err001.htm";
		}
		if(!st.hasQuestItems(Карта_Фортуны))
		{
			st.giveItem(Карта_Фортуны);
		}
		return "q10304_fortuna_map_start001.htm";
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025837, st.getInt("_1"));
			moblist.put(1025840, st.getInt("_2"));
			moblist.put(1025845, st.getInt("_3"));
			moblist.put(1025841, st.getInt("_4"));
			moblist.put(1025838, st.getInt("_5"));
			moblist.put(1025839, st.getInt("_6"));
			moblist.put(1025846, st.getInt("_7"));
			moblist.put(1025825, st.getInt("_8"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}