package dwo.scripts.quests;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.09.12
 * Time: 19:14
 */

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class _00420_LittleWing extends Quest
{
	// Квестовые персонажи
	private static final int Cooper = 30829;
	private static final int Cronos = 30610;
	private static final int Byron = 30711;
	private static final int Maria = 30608;
	private static final int Mimyu = 30747;
	private static final int Exarion = 30748;
	private static final int Zwov = 30749;
	private static final int Kalibran = 30750;
	private static final int Suzet = 30751;
	private static final int Shamhai = 30752;

	// Квестовые монстры
	private static final int Enchanted_Valey_First = 20589;
	private static final int Enchanted_Valey_Last = 20599;
	private static final int Toad_Lord = 20231;
	private static final int Marsh_Spider = 20233;
	private static final int Leto_Lizardman_Warrior = 20580;
	private static final int Road_Scavenger = 20551;
	private static final int Breka_Orc_Overlord = 20270;
	private static final int Dead_Seeker = 20202;

	// Предметы
	private static final short Coal = 1870;
	private static final short Charcoal = 1871;
	private static final short Silver_Nugget = 1873;
	private static final short Stone_of_Purity = 1875;
	private static final short GemstoneD = 2130;
	private static final short GemstoneC = 2131;
	private static final short Toad_Lord_Back_Skin = 3820;
	private static short[][] Fairy_Stone_Items = {
		{Coal, 10}, {Charcoal, 10}, {GemstoneD, 1}, {Silver_Nugget, 3}, {Toad_Lord_Back_Skin, 10}
	};
	private static short[][] Delux_Fairy_Stone_Items = {
		{Coal, 10}, {Charcoal, 10}, {GemstoneC, 1}, {Stone_of_Purity, 1}, {Silver_Nugget, 5}, {Toad_Lord_Back_Skin, 20}
	};
	private static final short Scale_of_Drake_Exarion = 3822;
	private static final short Scale_of_Drake_Zwov = 3824;
	private static final short Scale_of_Drake_Kalibran = 3826;
	private static final short Scale_of_Wyvern_Suzet = 3828;
	private static final short Scale_of_Wyvern_Shamhai = 3830;
	private static final short Egg_of_Drake_Exarion = 3823;
	private static final short Egg_of_Drake_Zwov = 3825;
	private static final short Egg_of_Drake_Kalibran = 3827;
	private static final short Egg_of_Wyvern_Suzet = 3829;
	private static final short Egg_of_Wyvern_Shamhai = 3831;
	private static final int[][] wyrms = {
		{Leto_Lizardman_Warrior, Exarion, Scale_of_Drake_Exarion, Egg_of_Drake_Exarion},
		{Marsh_Spider, Zwov, Scale_of_Drake_Zwov, Egg_of_Drake_Zwov},
		{Road_Scavenger, Kalibran, Scale_of_Drake_Kalibran, Egg_of_Drake_Kalibran},
		{Breka_Orc_Overlord, Suzet, Scale_of_Wyvern_Suzet, Egg_of_Wyvern_Suzet},
		{Dead_Seeker, Shamhai, Scale_of_Wyvern_Shamhai, Egg_of_Wyvern_Shamhai}
	};
	// Шансы
	private static final int Toad_Lord_Back_Skin_Chance = 30;
	private static final int Egg_Chance = 50;
	private static final int Pet_Armor_Chance = 35;
	private static short Hatchlings_Soft_Leather = 3912;
	private static short Food_For_Hatchling = 4038;
	// Квестовые предметыя
	private static short Fairy_Dust = 3499;
	private static short Fairy_Stone = 3816;
	private static short Deluxe_Fairy_Stone = 3817;
	private static short Fairy_Stone_List = 3818;
	private static short Deluxe_Fairy_Stone_List = 3819;
	private static short Juice_of_Monkshood = 3821;

	public _00420_LittleWing()
	{

		addStartNpc(Cooper);
		addTalkId(Cooper, Cronos, Mimyu, Byron, Maria);
		addKillId(Toad_Lord);

		for(int Enchanted_Valey_id = Enchanted_Valey_First; Enchanted_Valey_id <= Enchanted_Valey_Last; Enchanted_Valey_id++)
		{
			addKillId(Enchanted_Valey_id);
		}

		for(int[] wyrm : wyrms)
		{
			addTalkId(wyrm[1]);
			addKillId(wyrm[0]);
		}

		questItemIds = new int[]{
			Fairy_Dust, Fairy_Stone, Deluxe_Fairy_Stone, Fairy_Stone_List, Deluxe_Fairy_Stone_List, Toad_Lord_Back_Skin,
			Juice_of_Monkshood, Scale_of_Drake_Exarion, Scale_of_Drake_Zwov, Scale_of_Drake_Kalibran,
			Scale_of_Wyvern_Suzet, Scale_of_Wyvern_Shamhai, Egg_of_Drake_Exarion, Egg_of_Drake_Zwov,
			Egg_of_Drake_Kalibran, Egg_of_Wyvern_Suzet, Egg_of_Wyvern_Shamhai
		};
	}

	private static int getWyrmScale(int npc_id)
	{
		for(int[] wyrm : wyrms)
		{
			if(npc_id == wyrm[1])
			{
				return wyrm[2];
			}
		}
		return 0;
	}

	private static int getWyrmEgg(int npc_id)
	{
		for(int[] wyrm : wyrms)
		{
			if(npc_id == wyrm[1])
			{
				return wyrm[3];
			}
		}
		return 0;
	}

	private static int isWyrmStoler(int npc_id)
	{
		for(int[] wyrm : wyrms)
		{
			if(npc_id == wyrm[0])
			{
				return wyrm[1];
			}
		}
		return 0;
	}

	public static int getNeededSkins(QuestState st)
	{
		if(st.getQuestItemsCount(Deluxe_Fairy_Stone_List) > 0)
		{
			return 20;
		}
		if(st.getQuestItemsCount(Fairy_Stone_List) > 0)
		{
			return 10;
		}
		return -1;
	}

	public static boolean CheckFairyStoneItems(QuestState st, short[][] item_list)
	{
		for(short[] _item : item_list)
		{
			if(st.getQuestItemsCount(_item[0]) < _item[1])
			{
				return false;
			}
		}
		return true;
	}

	public static void TakeFairyStoneItems(QuestState st, short[][] item_list)
	{
		for(short[] _item : item_list)
		{
			st.takeItems(_item[0], _item[1]);
		}
	}

	public static void main(String[] args)
	{
		new _00420_LittleWing();
	}

	@Override
	public int getQuestId()
	{
		return 420;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept"))
		{
			st.startQuest();
			return "pet_manager_cooper_q0420_02.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Cronos)
		{
			if(st.isStarted())
			{
				switch(reply)
				{
					case 1:
						return "sage_cronos_q0420_02.htm";
					case 2:
						return "sage_cronos_q0420_03.htm";
					case 3:
						return "sage_cronos_q0420_04.htm";
					case 4:
						st.setCond(2);
						st.giveItems(Fairy_Stone_List, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "sage_cronos_q0420_05.htm";
					case 5:
						st.setCond(2);
						st.giveItems(Deluxe_Fairy_Stone_List, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "sage_cronos_q0420_06.htm";
					case 6:
						st.setCond(2);
						st.giveItems(Fairy_Stone_List, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "sage_cronos_q0420_12.htm";
					case 7:
						st.setCond(2);
						st.giveItems(Deluxe_Fairy_Stone_List, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "sage_cronos_q0420_13.htm";
				}
			}
		}
		else if(npcId == Maria)
		{
			if(st.isStarted())
			{
				if(reply == 1 && cond == 2)
				{
					if(st.hasQuestItems(Fairy_Stone_List) && CheckFairyStoneItems(st, Fairy_Stone_Items))
					{
						st.setCond(3);
						TakeFairyStoneItems(st, Fairy_Stone_Items);
						st.giveItems(Fairy_Stone, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "marya_q0420_03.htm";
					}
					else
					{
						return "marya_q0420_01.htm";
					}
				}
				else if(reply == 2 && cond == 2)
				{
					if(st.hasQuestItems(Deluxe_Fairy_Stone_List) && CheckFairyStoneItems(st, Delux_Fairy_Stone_Items))
					{
						st.setCond(3);
						TakeFairyStoneItems(st, Delux_Fairy_Stone_Items);
						st.giveItems(Deluxe_Fairy_Stone, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "marya_q0420_05.htm";
					}
					else
					{
						return "marya_q0420_01.htm";
					}
				}
			}
		}
		else if(npcId == Byron)
		{
			if(st.isStarted())
			{
				if(reply == 1)
				{
					return "guard_byron_q0420_02.htm";
				}
				else if(reply == 2 && cond == 3)
				{
					if(st.hasQuestItems(Fairy_Stone) || st.hasQuestItems(Deluxe_Fairy_Stone))
					{
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(st.hasQuestItems(Deluxe_Fairy_Stone))
						{
							return st.getInt("broken") == 1 ? "guard_byron_q0420_06.htm" : "guard_byron_q0420_04.htm";
						}
						if(st.getInt("broken") == 1)
						{
							return "guard_byron_q0420_05.htm";
						}
					}
				}
			}
		}
		else if(npcId == Mimyu)
		{
			if(st.isStarted())
			{
				if(reply == 1 && cond == 4)
				{
					if(st.hasQuestItems(Fairy_Stone))
					{
						st.takeItems(Fairy_Stone, -1);
						st.set("takedStone", "1");
						return "fairy_mymyu_q0420_03.htm";
					}
				}
				else if(reply == 2 && cond == 4)
				{
					return "fairy_mymyu_q0420_06.htm";
				}
				else if(reply == 3 && cond == 4)
				{
					if(st.hasQuestItems(Deluxe_Fairy_Stone))
					{
						st.takeItems(Deluxe_Fairy_Stone, -1);
						st.set("takedStone", "2");
						st.giveItems(Fairy_Dust, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						return "fairy_mymyu_q0420_05.htm";
					}
				}
				else if(reply == 4 && cond == 4)
				{
					st.setCond(5);
					st.unset("takedStone");
					st.giveItems(Juice_of_Monkshood, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "fairy_mymyu_q0420_08.htm";
				}
				else if(reply == 5 && cond == 7)
				{
					int egg_id = 0;
					for(int[] wyrm : wyrms)
					{
						if(st.getQuestItemsCount(wyrm[2]) == 0 && st.hasQuestItems(wyrm[3]))
						{
							egg_id = wyrm[3];
							break;
						}
					}
					if(egg_id == 0)
					{
						return getNoQuestMsg(player);
					}

					st.takeItems(egg_id, -1);

					st.giveItems(3500 + st.getRandom(3), 1);
					if(st.hasQuestItems(Fairy_Dust))
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "fairy_mymyu_q0420_13.htm";
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "fairy_mymyu_q0420_16.htm";
				}
				else if(reply == 6 && cond == 7)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					if(!st.hasQuestItems(Fairy_Dust))
					{
						return "fairy_mymyu_q0420_14.htm";
					}
					st.takeItems(Fairy_Dust, -1);
					if(Rnd.getChance(Pet_Armor_Chance))
					{
						st.giveItems(Hatchlings_Soft_Leather, 1);
						return "fairy_mymyu_q0420_15.htm";
					}
					else
					{
						st.giveItems(Food_For_Hatchling, 20);
						return "fairy_mymyu_q0420_15t.htm";
					}
				}
				else if(reply == 7 && cond == 7)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "fairy_mymyu_q0420_14.htm";
				}
			}
		}
		else if(npcId == Exarion)
		{
			if(reply == 1 && cond == 5)
			{
				if(st.hasQuestItems(Juice_of_Monkshood))
				{
					st.takeItems(Juice_of_Monkshood, -1);
					st.setCond(6);
					st.giveItems(3822, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "drake_exarion_q0420_03.htm";
				}
			}
		}
		else if(npcId == Zwov)
		{
			if(reply == 1 && cond == 5)
			{
				if(st.hasQuestItems(Juice_of_Monkshood))
				{
					st.takeItems(Juice_of_Monkshood, -1);
					st.setCond(6);
					st.giveItems(3824, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "drake_zwov_q0420_03.htm";
				}
			}
		}
		else if(npcId == Kalibran)
		{
			if(reply == 1 && cond == 5)
			{
				if(st.hasQuestItems(Juice_of_Monkshood))
				{
					st.takeItems(Juice_of_Monkshood, -1);
					st.setCond(6);
					st.giveItems(3826, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "drake_kalibran_q0420_03.htm";
				}
			}
		}
		else if(npcId == Suzet)
		{
			if(reply == 1 && cond == 5)
			{
				if(st.hasQuestItems(Juice_of_Monkshood))
				{
					st.takeItems(Juice_of_Monkshood, -1);
					st.setCond(6);
					st.giveItems(3828, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "wyrm_suzet_q0420_03.htm";
				}
			}
		}
		else if(npcId == Shamhai)
		{
			if(reply == 1 && cond == 5)
			{
				if(st.hasQuestItems(Juice_of_Monkshood))
				{
					st.takeItems(Juice_of_Monkshood, -1);
					st.setCond(6);
					st.giveItems(3830, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "wyrm_shamhai_q0420_03.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int wyrm_id = isWyrmStoler(npcId);

		if(npcId == Toad_Lord)
		{
			if(cond == 2)
			{
				int needed_skins = getNeededSkins(st);
				if(st.getQuestItemsCount(Toad_Lord_Back_Skin) < needed_skins && Rnd.getChance(Toad_Lord_Back_Skin_Chance))
				{
					st.giveItems(Toad_Lord_Back_Skin, 1);
					st.playSound(st.getQuestItemsCount(Toad_Lord_Back_Skin) < needed_skins ? QuestSound.ITEMSOUND_QUEST_ITEMGET : QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		else if(npcId >= Enchanted_Valey_First && npcId <= Enchanted_Valey_Last)
		{
			if(Rnd.getChance(30) && st.hasQuestItems(Deluxe_Fairy_Stone))
			{
				st.takeItems(Deluxe_Fairy_Stone, 1);
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npcId, NpcStringId.THE_STONE_THE_ELVEN_STONE_BROKE));
				st.set("broken", "1");
				st.setCond(1);
			}
		}
		else if(wyrm_id > 0)
		{
			if(cond == 6 && st.getQuestItemsCount(getWyrmScale(wyrm_id)) > 0 && st.getQuestItemsCount(getWyrmEgg(wyrm_id)) < 20 && Rnd.getChance(Egg_Chance))
			{
				st.giveItems(getWyrmEgg(wyrm_id), 1);
				st.playSound(st.getQuestItemsCount(getWyrmEgg(wyrm_id)) < 20 ? QuestSound.ITEMSOUND_QUEST_ITEMGET : QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int broken = st.getInt("broken");

		if(npcId == Cooper)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() < 35)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "pet_manager_cooper_q0420_03.htm";
					}
					else
					{
						return "pet_manager_cooper_q0420_01.htm";
					}
				case STARTED:
					return cond == 1 ? "pet_manager_cooper_q0420_02.htm" : "pet_manager_cooper_q0420_04.htm";
			}
		}
		else if(npcId == Cronos)
		{
			if(st.isStarted())
			{
				switch(cond)
				{
					case 1:
						return broken == 1 ? "sage_cronos_q0420_10.htm" : "sage_cronos_q0420_01.htm";
					case 2:
						return "sage_cronos_q0420_07.htm";
					case 3:
						return broken == 1 ? "sage_cronos_q0420_14.htm" : "sage_cronos_q0420_08.htm";
					case 4:
						return "sage_cronos_q0420_09.htm";
					default:
						return "sage_cronos_q0420_11.htm";
				}
			}
		}
		else if(npcId == Maria)
		{
			if(cond == 2)
			{
				if(st.hasQuestItems(Deluxe_Fairy_Stone_List))
				{
					return CheckFairyStoneItems(st, Delux_Fairy_Stone_Items) ? "marya_q0420_04.htm" : "marya_q0420_01.htm";
				}
				else if(st.hasQuestItems(Fairy_Stone_List))
				{
					return CheckFairyStoneItems(st, Fairy_Stone_Items) ? "marya_q0420_02.htm" : "marya_q0420_01.htm";
				}
			}
			else if(cond > 2)
			{
				return "marya_q0420_06.htm";
			}
		}
		else if(npcId == Byron)
		{
			if(st.isStarted())
			{
				switch(cond)
				{
					case 1:
						if(broken == 1)
						{
							return "guard_byron_q0420_09.htm";
						}
					case 2:
						if(broken == 1)
						{
							return "guard_byron_q0420_10.htm";
						}
					case 3:
						if(st.hasQuestItems(Fairy_Stone) || st.hasQuestItems(Deluxe_Fairy_Stone))
						{
							return "guard_byron_q0420_01.htm";
						}
					default: // cond >= 4
						if(st.hasQuestItems(Deluxe_Fairy_Stone))
						{
							return "guard_byron_q0420_08.htm";
						}
						else if(st.hasQuestItems(Fairy_Stone))
						{
							return "guard_byron_q0420_07.htm";
						}
				}
			}
		}
		else if(npcId == Mimyu)
		{
			if(st.isStarted())
			{
				switch(cond)
				{
					case 4:
						if(st.hasQuestItems(Deluxe_Fairy_Stone))
						{
							return "fairy_mymyu_q0420_04.htm";
						}
						if(st.hasQuestItems(Fairy_Stone))
						{
							return "fairy_mymyu_q0420_02.htm";
						}
						if(st.getInt("takedStone") > 0)
						{
							return "fairy_mymyu_q0420_07.htm";
						}
					case 5:
						return "fairy_mymyu_q0420_09.htm";
					case 6:
						for(int[] wyrm : wyrms)
						{
							if(!st.hasQuestItems(wyrm[2]) && st.getQuestItemsCount(wyrm[3]) >= 20)
							{
								return "fairy_mymyu_q0420_11.htm";
							}
						}
						return "fairy_mymyu_q0420_10.htm";
					case 7:
						for(int[] wyrm : wyrms)
						{
							if(st.getQuestItemsCount(wyrm[2]) == 0 && st.getQuestItemsCount(wyrm[3]) >= 1)
							{
								return "fairy_mymyu_q0420_12.htm";
							}
						}
				}
			}
		}
		else if(npcId >= Exarion && npcId <= Shamhai)
		{
			if(cond == 5 && st.hasQuestItems(Juice_of_Monkshood))
			{
				return npc.getServerName() + "_q0420_02.htm";
			}
			else if(cond == 6 && st.hasQuestItems(getWyrmScale(npcId)))
			{
				int egg_id = getWyrmEgg(npcId);
				if(st.getQuestItemsCount(egg_id) < 20)
				{
					return npc.getServerName() + "_q0420_04.htm";
				}
				st.takeItems(getWyrmScale(npcId), -1);
				st.takeItems(egg_id, -1);
				st.giveItems(egg_id, 1);
				st.setCond(7);
				return npc.getServerName() + "_q0420_05.htm";
			}
			else if(cond == 7 && st.hasQuestItems(getWyrmEgg(npcId)))
			{
				return npc.getServerName() + "_q0420_06.htm";
			}
		}

		return getNoQuestMsg(player);
	}
}