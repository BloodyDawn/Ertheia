package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.08.12
 * Time: 5:21
 * TODO: Обмен монеток у НПЦ
 */

public class _00336_CoinsofMagic extends Quest
{
	// Квестовые предметы
	private static final int COIN_DIAGRAM = 3811;
	private static final int KALDIS_COIN = 3812;
	private static final int MEMBERSHIP_1 = 3813;
	private static final int MEMBERSHIP_2 = 3814;
	private static final int MEMBERSHIP_3 = 3815;

	// Квестовые монстры
	private static final int BLOOD_MEDUSA = 3472;
	private static final int BLOOD_WEREWOLF = 3473;
	private static final int BLOOD_BASILISK = 3474;
	private static final int BLOOD_DREVANUL = 3475;
	private static final int BLOOD_SUCCUBUS = 3476;
	private static final int BLOOD_DRAGON = 3477;
	private static final int BELETHS_BLOOD = 3478;
	private static final int MANAKS_BLOOD_WEREWOLF = 3479;
	private static final int NIAS_BLOOD_MEDUSA = 3480;
	private static final int GOLD_DRAGON = 3481;
	private static final int GOLD_WYVERN = 3482;
	private static final int GOLD_KNIGHT = 3483;
	private static final int GOLD_GIANT = 3484;
	private static final int GOLD_DRAKE = 3485;
	private static final int GOLD_WYRM = 3486;
	private static final int BELETHS_GOLD = 3487;
	private static final int MANAKS_GOLD_GIANT = 3488;
	private static final int NIAS_GOLD_WYVERN = 3489;
	private static final int SILVER_UNICORN = 3490;
	private static final int[] BASIC_COINS = {
		BLOOD_MEDUSA, GOLD_WYVERN, SILVER_UNICORN
	};
	private static final int SILVER_FAIRY = 3491;
	private static final int SILVER_DRYAD = 3492;
	private static final int SILVER_DRAGON = 3493;
	private static final int SILVER_GOLEM = 3494;
	private static final int SILVER_UNDINE = 3495;
	private static final int[][] PROMOTE = {
		{}, {}, {
		SILVER_DRYAD, BLOOD_BASILISK, BLOOD_SUCCUBUS, SILVER_UNDINE, GOLD_GIANT, GOLD_WYRM
	}, {
		BLOOD_WEREWOLF, GOLD_DRAKE, SILVER_FAIRY, BLOOD_DREVANUL, GOLD_KNIGHT, SILVER_GOLEM
	}
	};
	private static final int BELETHS_SILVER = 3496;
	private static final int MANAKS_SILVER_DRYAD = 3497;
	private static final int NIAS_SILVER_FAIRY = 3498;
	private static final int SORINT = 30232;
	private static final int BERNARD = 30702;
	private static final int PAGE = 30696;
	private static final int HAGGER = 30183;
	private static final int STAN = 30200;
	private static final int RALFORD = 30165;
	private static final int FERRIS = 30847;
	private static final int COLLOB = 30092;
	private static final int PANO = 30078;
	private static final int DUNING = 30688;
	private static final int LORAIN = 30673;
	private static final int[][] EXCHANGE_LEVEL = {
		{
			PAGE, 3
		}, {
		LORAIN, 3
	}, {
		HAGGER, 3
	}, {
		RALFORD, 2
	}, {
		STAN, 2
	}, {
		DUNING, 2
	}, {
		FERRIS, 1
	}, {
		COLLOB, 1
	}, {
		PANO, 1
	},
	};
	// НПЦ для обмена монеток
	private static final int[] CHANGE_NPCS = {PAGE, HAGGER, STAN, RALFORD, FERRIS, COLLOB, PANO, DUNING, LORAIN};
	private static final int TimakOrcArcher = 20584;
	private static final int TimakOrcSoldier = 20585;
	private static final int TimakOrcShaman = 20587;
	private static final int Lakin = 20604;
	private static final int TorturedUndead = 20678;
	private static final int HatarHanishee = 20663;
	private static final int Shackle = 20235;
	private static final int TimakOrc = 20583;
	private static final int HeadlessKnight = 20146;
	private static final int RoyalCaveServant = 20240;
	private static final int MalrukSuccubusTuren = 20245;
	private static final int Formor = 20568;
	private static final int FormorElder = 20569;
	private static final int VanorSilenosShaman = 20685;
	private static final int TarlkBugbearHighWarrior = 20572;
	private static final int OelMahum = 20161;
	private static final int OelMahumWarrior = 20575;
	private static final int[][] DROPLIST = {
		{
			TimakOrcArcher, BLOOD_MEDUSA
		}, {
		TimakOrcSoldier, BLOOD_MEDUSA
	}, {
		TimakOrcShaman, BLOOD_MEDUSA
	}, {
		Lakin, BLOOD_MEDUSA
	}, {
		TorturedUndead, BLOOD_MEDUSA
	}, {
		HatarHanishee, BLOOD_MEDUSA
	},

		{
			TimakOrc, GOLD_WYVERN
		}, {
		Shackle, GOLD_WYVERN
	}, {
		HeadlessKnight, GOLD_WYVERN
	}, {
		RoyalCaveServant, GOLD_WYVERN
	}, {
		MalrukSuccubusTuren, GOLD_WYVERN
	},

		{
			Formor, SILVER_UNICORN
		}, {
		FormorElder, SILVER_UNICORN
	}, {
		VanorSilenosShaman, SILVER_UNICORN
	}, {
		TarlkBugbearHighWarrior, SILVER_UNICORN
	}, {
		OelMahum, SILVER_UNICORN
	}, {
		OelMahumWarrior, SILVER_UNICORN
	},
	};
	private static final int HaritLizardmanMatriarch = 20645;
	private static final int HaritLizardmanShaman = 20644;
	// not spawned
	private static final int Shackle2 = 20279;
	private static final int HeadlessKnight2 = 20280;
	private static final int MalrukSuccubusTuren2 = 20284;
	private static final int RoyalCaveServant2 = 20276;
	// New
	private static final int GraveLich = 21003;
	private static final int DoomServant = 21006;
	private static final int DoomArcher = 21008;
	private static final int DoomKnight = 20674;
	//private static final int Kookaburra1 = 21277;
	private static final int Kookaburra2 = 21276;
	private static final int Kookaburra3 = 21275;
	private static final int Kookaburra4 = 21274;
	//private static final int Antelope1 = 21281;
	private static final int Antelope2 = 21278;
	private static final int Antelope3 = 21279;
	private static final int Antelope4 = 21280;
	//private static final int Bandersnatch1 = 21285;
	private static final int Bandersnatch2 = 21282;
	private static final int Bandersnatch3 = 21284;
	private static final int Bandersnatch4 = 21283;
	//private static final int Buffalo1 = 21289;
	private static final int Buffalo2 = 21287;
	private static final int Buffalo3 = 21288;
	private static final int Buffalo4 = 21286;
	private static final int ClawsofSplendor = 21521;
	private static final int WisdomofSplendor = 21526;
	private static final int PunishmentofSplendor = 21531;
	private static final int WailingofSplendor = 21539;
	private static final int HungeredCorpse = 20954;
	private static final int BloodyGhost = 20960;
	private static final int NihilInvader = 20957;
	private static final int DarkGuard = 20959;
	private static final int[] UNKNOWN = {
		GraveLich, DoomServant, DoomArcher, DoomKnight,
		//Kookaburra1,
		Kookaburra2, Kookaburra3, Kookaburra4,
		//Antelope1,
		Antelope2, Antelope3, Antelope4,
		//Bandersnatch1,
		Bandersnatch2, Bandersnatch3, Bandersnatch4,
		//Buffalo1,
		Buffalo2, Buffalo3, Buffalo4, ClawsofSplendor, WisdomofSplendor, PunishmentofSplendor, WailingofSplendor,
		HungeredCorpse, BloodyGhost, NihilInvader, DarkGuard
	};

	public _00336_CoinsofMagic()
	{
		addStartNpc(SORINT);
		addTalkId(SORINT, BERNARD, PAGE, HAGGER, STAN, RALFORD, FERRIS, COLLOB, PANO, DUNING, LORAIN);

		for(int[] mob : DROPLIST)
		{
			addKillId(mob[0]);
		}
		addKillId(UNKNOWN);
		addKillId(HaritLizardmanMatriarch, HaritLizardmanShaman);

		questItemIds = new int[]{COIN_DIAGRAM, KALDIS_COIN, MEMBERSHIP_1, MEMBERSHIP_2, MEMBERSHIP_3};
	}

	public static void main(String[] args)
	{
		new _00336_CoinsofMagic();
	}

	private String promoteGrade(QuestState st)
	{
		int grade = st.getInt("grade");
		if(grade == 1)
		{
			return "warehouse_keeper_sorint_q0336_15.htm";
		}
		else
		{
			int h = 0;
			for(int i : PROMOTE[grade])
			{
				if(st.getQuestItemsCount(i) > 0)
				{
					h += 1;
				}
			}
			if(h == 6)
			{
				for(int i : PROMOTE[grade])
				{
					st.takeItems(i, 1);
				}
				st.takeItems(3812 + grade, -1);
				st.giveItems(3811 + grade, 1);
				st.set("grade", String.valueOf(grade - 1));
				if(grade == 3)
				{
					st.setCond(9);
				}
				else if(grade == 2)
				{
					st.setCond(11);
				}
				st.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
				return "warehouse_keeper_sorint_q0336_" + (19 - grade) + ".htm";
			}
			else
			{
				if(grade == 3)
				{
					st.setCond(8);
				}
				else if(grade == 2)
				{
					st.setCond(9);
				}
				return "warehouse_keeper_sorint_q0336_" + (16 - grade) + ".htm";
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 336;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			qs.giveItems(COIN_DIAGRAM, 1);
			return "warehouse_keeper_sorint_q0336_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == SORINT)
		{
			if(reply == 1)
			{
				return "warehouse_keeper_sorint_q0336_03.htm";
			}
			else if(reply == 2)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "warehouse_keeper_sorint_q0336_04.htm";
			}
			else if(reply == 3)
			{
				return "warehouse_keeper_sorint_q0336_08.htm";
			}
			else if(reply == 4) // Назад
			{
				return onTalk(npc, st);
			}
			else if(reply == 5) // Я хотел бы получить более высокий уровень
			{
				return promoteGrade(st);
			}
			else if(reply == 6) // Я хотел бы выйти из гильдии
			{
				return "warehouse_keeper_sorint_q0336_18.htm";
			}
			else if(reply == 10) // Где берут монеты?
			{
				if(cond < 6)
				{
					st.setCond(6);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				return "warehouse_keeper_sorint_q0336_22.htm";
			}
			else if(reply == 11) // Я хотел бы узнать о членах Гильдии Нумизматов
			{
				if(cond < 5)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				return "warehouse_keeper_sorint_q0336_23.htm";
			}
			else if(reply == 100) // Я хочу выйти из гильдии нумизматов
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "warehouse_keeper_sorint_q0336_18a.htm";
			}
		}
		else if(npcId == BERNARD)
		{
			if(reply == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "union_president_bernard_q0336_02.htm";
			}
			else if(reply == 2)
			{
				return "union_president_bernard_q0336_03.htm";
			}
			else if(reply == 3)
			{
				return "union_president_bernard_q0336_04.htm";
			}
			else if(reply == 4)
			{
				if(cond < 7)
				{
					st.setCond(7);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				return "union_president_bernard_q0336_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		int grade = st.getInt("grade");
		int chance = npc.getLevel() + grade * 3 - 20;
		int npcId = npc.getNpcId();
		if(npcId == HaritLizardmanMatriarch || npcId == HaritLizardmanShaman)
		{
			if(cond == 2)
			{
				if(st.rollAndGive(KALDIS_COIN, 1, 1, 1, 10 * Config.RATE_QUEST_DROP))
				{
					st.setCond(3);
				}
			}
			return null;
		}
		for(int[] e : DROPLIST)
		{
			if(e[0] == npcId)
			{
				st.rollAndGive(e[1], 1, chance);
				return null;
			}
		}
		for(int u : UNKNOWN)
		{
			if(u == npcId)
			{
				st.rollAndGive(BASIC_COINS[Rnd.get(BASIC_COINS.length)], 1, chance * Config.RATE_QUEST_DROP);
				return null;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int grade = st.getInt("grade");

		if(npcId == SORINT)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() < 40)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "warehouse_keeper_sorint_q0336_01.htm";
					}
					else
					{
						return "warehouse_keeper_sorint_q0336_02.htm";
					}
				case STARTED:
					if(st.hasQuestItems(COIN_DIAGRAM))
					{
						if(st.hasQuestItems(KALDIS_COIN))
						{
							st.takeItems(KALDIS_COIN, -1);
							st.takeItems(COIN_DIAGRAM, -1);
							st.giveItems(MEMBERSHIP_3, 1);
							st.set("grade", "3");
							st.setCond(4);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
							return "warehouse_keeper_sorint_q0336_07.htm";
						}
						else
						{
							return "warehouse_keeper_sorint_q0336_06.htm";
						}
					}
					else if(grade == 3)
					{
						return "warehouse_keeper_sorint_q0336_12.htm";
					}
					else if(grade == 2)
					{
						return "warehouse_keeper_sorint_q0336_11.htm";
					}
					else if(grade == 1)
					{
						return "warehouse_keeper_sorint_q0336_10.htm";
					}
			}
		}
		else if(npcId == BERNARD)
		{
			if(st.isStarted())
			{
				if(st.hasQuestItems(COIN_DIAGRAM) && grade == 0)
				{
					return "union_president_bernard_q0336_01.htm";
				}
				else if(grade == 3)
				{
					return "union_president_bernard_q0336_05.htm";
				}
			}
		}
		else if(ArrayUtils.contains(CHANGE_NPCS, npcId))
		{
			if(st.isStarted())
			{
				for(int[] e : EXCHANGE_LEVEL)
				{
					if(npcId == e[0] && grade <= e[1])
					{
						return npc.getServerName() + "_q0336_01.htm";
					}
				}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}