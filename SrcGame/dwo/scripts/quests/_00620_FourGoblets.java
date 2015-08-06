package dwo.scripts.quests;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.FourSepulchersManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.12.12
 * Time: 7:34
 */

public class _00620_FourGoblets extends Quest
{
	public static final int Sealed_Box = 7255;
	// Замки
	private static final int[] LOCKS = {
		31925, 31926, 31927, 31928, 31929, 31930, 31931, 31932, 31933, 31934, 31935, 31936, 31937, 31938, 31939, 31940,
		31941, 31942, 31943, 31944
	};
	// Триггер-боксы
	private static final int[] TRIGGER_BOXES = {
		31468, 31469, 31470, 31471, 31472, 31473, 31474, 31475, 31476, 31477, 31478, 31479, 31480, 31481, 31482, 31483,
		31484, 31485, 31486, 31487
	};
	// Кей-боксы
	private static final int[] KEY_BOXES = {
		31455, 31456, 31457, 31458, 31459, 31460, 31461, 31462, 31463, 31464, 31465, 31466, 31467
	};
	private static final int GOBLET1 = 7256;
	private static final int GOBLET2 = 7257;
	private static final int GOBLET3 = 7258;
	private static final int GOBLET4 = 7259;
	// Квестовые персонажи
	private static int NAMELESS_SPIRIT = 31453;
	private static int GHOST_OF_WIGOTH_1 = 31452;
	private static int GHOST_OF_WIGOTH_2 = 31454;
	private static int CONQ_SM = 31921;
	private static int EMPER_SM = 31922;
	private static int SAGES_SM = 31923;
	private static int JUDGE_SM = 31924;
	private static int GHOST_CHAMBERLAIN_1 = 31919;
	private static int GHOST_CHAMBERLAIN_2 = 31920;
	// Квестовые предметы
	private static int GRAVE_PASS = 7261;
	private static int RELIC = 7254;
	// Награды
	private static int ANTIQUE_BROOCH = 7262;
	private static int[] RCP_REWARDS = {
		6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580
	};

	public _00620_FourGoblets()
	{
		addStartNpc(NAMELESS_SPIRIT);
		addTalkId(GHOST_OF_WIGOTH_1, GHOST_OF_WIGOTH_2, CONQ_SM, EMPER_SM, SAGES_SM, JUDGE_SM, GHOST_CHAMBERLAIN_1, GHOST_CHAMBERLAIN_2);
		addTalkId(LOCKS);
		addFirstTalkId(TRIGGER_BOXES);
		addFirstTalkId(KEY_BOXES);
		questItemIds = new int[]{Sealed_Box, GRAVE_PASS, GOBLET1, GOBLET2, GOBLET3, GOBLET4};

		for(int id = 18120; id <= 18256; id++)
		{
			addKillId(id);
		}
	}

	public static void main(String[] args)
	{
		new _00620_FourGoblets();
	}

	@Override
	public int getQuestId()
	{
		return 620;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			if(qs.getPlayer().getItemsCount(7262) >= 1)
			{
				qs.setCond(2);
			}
			return "printessa_spirit_q0620_13.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == NAMELESS_SPIRIT)
		{
			switch(reply)
			{
				case 1:
					st.takeItems(7260, -1);
					st.takeItems(7261, -1);
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "printessa_spirit_q0620_18.htm";
				case 2:
					return "printessa_spirit_q0620_19.htm";
				case 3:
					return "printessa_spirit_q0620_20.htm";
				case 4:
					if(player.getItemsCount(7262) == 0 && player.getItemsCount(7256) >= 1 && player.getItemsCount(7257) >= 1 && player.getItemsCount(7258) >= 1 && player.getItemsCount(7259) >= 1)
					{
						st.giveItems(7262, 1);
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.takeItems(7256, 1);
						st.takeItems(7257, 1);
						st.takeItems(7258, 1);
						st.takeItems(7259, 1);
						return "printessa_spirit_q0620_16.htm";
					}
					break;
				case 5:
					return "printessa_spirit_q0620_02.htm";
				case 6:
					return "printessa_spirit_q0620_03.htm";
				case 7:
					return "printessa_spirit_q0620_04.htm";
				case 8:
					return "printessa_spirit_q0620_05.htm";
				case 9:
					return "printessa_spirit_q0620_06.htm";
				case 10:
					return "printessa_spirit_q0620_07.htm";
				case 11:
					return "printessa_spirit_q0620_08.htm";
				case 12:
					return "printessa_spirit_q0620_09.htm";
				case 13:
					return "printessa_spirit_q0620_10.htm";
				case 14:
					return "printessa_spirit_q0620_11.htm";
			}
		}
		else if(npc.getNpcId() == GHOST_OF_WIGOTH_1)
		{
			if(reply == 1)
			{
				return "wigoth_ghost_a_q0620_03.htm";
			}
			if(reply == 2)
			{
				st.takeItems(7260, -1);
				player.teleToLocation(169584, -91008, -2912);
			}
		}
		else if(npc.getNpcId() == GHOST_OF_WIGOTH_2)
		{
			if(reply == 1)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7255) >= 1)
				{
					if(Rnd.getChance(100))
					{
						int i0 = Rnd.get(5);
						int i2 = 0;
						if(i0 == 0)
						{
							i2 = 1;
							st.giveAdena(10000, true);
						}
						else if(i0 == 1)
						{
							if(Rnd.get(1000) < 848)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 43)
								{
									st.giveItems(1884, 42);
								}
								else if(i1 < 66)
								{
									st.giveItems(1895, 36);
								}
								else if(i1 < 184)
								{
									st.giveItems(1876, 4);
								}
								else if(i1 < 250)
								{
									st.giveItems(1881, 6);
								}
								else if(i1 < 287)
								{
									st.giveItems(5549, 8);
								}
								else if(i1 < 484)
								{
									st.giveItems(1874, 1);
								}
								else if(i1 < 681)
								{
									st.giveItems(1889, 1);
								}
								else if(i1 < 799)
								{
									st.giveItems(1877, 1);
								}
								else if(i1 < 902)
								{
									st.giveItems(1894, 1);
								}
								else
								{
									st.giveItems(4043, 1);
								}
							}
							if(Rnd.get(1000) < 323)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 335)
								{
									st.giveItems(1888, 1);
								}
								else if(i1 < 556)
								{
									st.giveItems(4040, 1);
								}
								else if(i1 < 725)
								{
									st.giveItems(1890, 1);
								}
								else if(i1 < 872)
								{
									st.giveItems(5550, 1);
								}
								else if(i1 < 962)
								{
									st.giveItems(1893, 1);
								}
								else if(i1 < 986)
								{
									st.giveItems(4046, 1);
								}
								else
								{
									st.giveItems(4048, 1);
								}
							}
						}
						else if(i0 == 2)
						{
							if(Rnd.get(1000) < 847)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 148)
								{
									st.giveItems(1878, 8);
								}
								else if(i1 < 175)
								{
									st.giveItems(1882, 24);
								}
								else if(i1 < 273)
								{
									st.giveItems(1879, 4);
								}
								else if(i1 < 322)
								{
									st.giveItems(1880, 6);
								}
								else if(i1 < 357)
								{
									st.giveItems(1885, 6);
								}
								else if(i1 < 554)
								{
									st.giveItems(1875, 1);
								}
								else if(i1 < 685)
								{
									st.giveItems(1883, 1);
								}
								else if(i1 < 803)
								{
									st.giveItems(5220, 1);
								}
								else if(i1 < 901)
								{
									st.giveItems(4039, 1);
								}
								else
								{
									st.giveItems(4044, 1);
								}
							}
							if(Rnd.get(1000) < 251)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 350)
								{
									st.giveItems(1887, 1);
								}
								else if(i1 < 587)
								{
									st.giveItems(4042, 1);
								}
								else if(i1 < 798)
								{
									st.giveItems(1886, 1);
								}
								else if(i1 < 922)
								{
									st.giveItems(4041, 1);
								}
								else if(i1 < 966)
								{
									st.giveItems(1892, 1);
								}
								else if(i1 < 996)
								{
									st.giveItems(1891, 1);
								}
								else
								{
									st.giveItems(4047, 1);
								}
							}
						}
						else if(i0 == 3)
						{
							if(Rnd.get(1000) < 31)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 223)
								{
									st.giveItems(730, 1);
								}
								else if(i1 < 893)
								{
									st.giveItems(948, 1);
								}
								else
								{
									st.giveItems(960, 1);
								}
							}
							if(Rnd.get(1000) < 5)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 202)
								{
									st.giveItems(729, 1);
								}
								else if(i1 < 928)
								{
									st.giveItems(947, 1);
								}
								else
								{
									st.giveItems(959, 1);
								}
							}
						}
						else if(i0 == 4)
						{
							if(Rnd.get(1000) < 329)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 88)
								{
									st.giveItems(6698, 1);
								}
								else if(i1 < 185)
								{
									st.giveItems(6699, 1);
								}
								else if(i1 < 238)
								{
									st.giveItems(6700, 1);
								}
								else if(i1 < 262)
								{
									st.giveItems(6701, 1);
								}
								else if(i1 < 292)
								{
									st.giveItems(6702, 1);
								}
								else if(i1 < 356)
								{
									st.giveItems(6703, 1);
								}
								else if(i1 < 420)
								{
									st.giveItems(6704, 1);
								}
								else if(i1 < 482)
								{
									st.giveItems(6705, 1);
								}
								else if(i1 < 554)
								{
									st.giveItems(6706, 1);
								}
								else if(i1 < 576)
								{
									st.giveItems(6707, 1);
								}
								else if(i1 < 640)
								{
									st.giveItems(6708, 1);
								}
								else if(i1 < 704)
								{
									st.giveItems(6709, 1);
								}
								else if(i1 < 777)
								{
									st.giveItems(6710, 1);
								}
								else if(i1 < 799)
								{
									st.giveItems(6711, 1);
								}
								else if(i1 < 863)
								{
									st.giveItems(6712, 1);
								}
								else if(i1 < 927)
								{
									st.giveItems(6713, 1);
								}
								else
								{
									st.giveItems(6714, 1);
								}
							}
							if(Rnd.get(1000) < 54)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 100)
								{
									st.giveItems(6688, 1);
								}
								else if(i1 < 198)
								{
									st.giveItems(6689, 1);
								}
								else if(i1 < 298)
								{
									st.giveItems(6690, 1);
								}
								else if(i1 < 398)
								{
									st.giveItems(6691, 1);
								}
								else if(i1 < 499)
								{
									st.giveItems(7579, 1);
								}
								else if(i1 < 601)
								{
									st.giveItems(6693, 1);
								}
								else if(i1 < 703)
								{
									st.giveItems(6694, 1);
								}
								else if(i1 < 801)
								{
									st.giveItems(6695, 1);
								}
								else if(i1 < 902)
								{
									st.giveItems(6696, 1);
								}
								else
								{
									st.giveItems(6697, 1);
								}
							}
						}
						st.takeItems(7255, 1);
						if(i2 == 1)
						{
							return "wigoth_ghost_b_q0620_13.htm";
						}
						else if(i2 == 0)
						{
							return "wigoth_ghost_b_q0620_14.htm";
						}
					}
					else
					{
						st.takeItems(7255, 1);
						return "wigoth_ghost_b_q0620_15.htm";
					}
				}
			}
			else if(reply == 2)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					return "wigoth_ghost_b_q0620_16.htm";
				}
			}
			else if(reply == 3)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6881, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_17.htm";
				}
			}
			else if(reply == 4)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6883, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_18.htm";
				}
			}
			else if(reply == 5)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6885, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_19.htm";
				}
			}
			else if(reply == 6)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6887, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_20.htm";
				}
			}
			else if(reply == 7)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(7580, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_21.htm";
				}
			}
			else if(reply == 8)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6891, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_22.htm";
				}
			}
			else if(reply == 9)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6893, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_23.htm";
				}
			}
			else if(reply == 10)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6895, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_24.htm";
				}
			}
			else if(reply == 11)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6897, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_25.htm";
				}
			}
			else if(reply == 12)
			{
				if((st.getMemoStateEx(1) == 2 || st.getMemoStateEx(1) == 3) && player.getItemsCount(7254) >= 1000)
				{
					st.giveItems(6899, 1);
					st.takeItems(7254, 1000);
					return "wigoth_ghost_b_q0620_26.htm";
				}
			}
			else if(reply == 13)
			{
				player.teleToLocation(170000, -88250, -2912);
				return "wigoth_ghost_b_q0620_01a.htm";
			}
		}
		else if(npc.getNpcId() == CONQ_SM || npc.getNpcId() == EMPER_SM || npc.getNpcId() == SAGES_SM || npc.getNpcId() == JUDGE_SM)
		{
			if(reply == 1)
			{
				if(!player.isInParty() || player.getParty().getMemberCount() < 4)
				{
					return npc.getServerName() + "_q0620_04.htm";
				}
				L2PcInstance partyLeader = player.getParty().getLeader();
				if(!FourSepulchersManager.getInstance().isEntryTime())
				{
					return npc.getServerName() + "_q0620_02.htm";
				}
				else if(!player.equals(partyLeader))
				{
					return npc.getServerName() + "_q0620_03.htm";
				}
				else
				{
					for(L2PcInstance partyMember : partyLeader.getParty().getMembers())
					{
						if(partyMember.getItemsCount(7075) == 0)
						{
							String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + npc.getServerName() + "_q0620_05.htm");
							content = content.replace("member1", partyMember.getName());
							return content;
						}
						else if(partyLeader.getQuestState(getClass()) == null)
						{
							String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + npc.getServerName() + "_q0620_06.htm");
							content = content.replace("member1", partyMember.getName());
							return content;
						}
					}
					if(FourSepulchersManager.getInstance().isHallInUse(npc.getNpcId()))
					{
						return npc.getServerName() + "_q0620_07.htm";
					}
					else
					{
						for(L2PcInstance partyMember : player.getParty().getMembers())
						{
							QuestState pst = partyMember.getQuestState(getClass());
							if(partyMember.getItemsCount(7262) == 0)
							{
								partyMember.addItem(ProcessType.NPC, 7261, 1, npc, true);
							}
							partyMember.destroyItemByItemId(ProcessType.NPC, 7075, 1, npc, true);
							partyMember.destroyItemByItemId(ProcessType.NPC, 7260, partyMember.getItemsCount(7260), npc, true);
							pst.setMemoStateEx(1, 1);
						}
						FourSepulchersManager.getInstance().entry(npc.getNpcId(), player);
					}
				}
			}
		}
		else if(npc.getNpcId() == GHOST_CHAMBERLAIN_1)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(7255) >= 1)
				{
					if(Rnd.getChance(50))
					{
						int i0 = Rnd.get(5);
						int i2 = 0;
						if(i0 == 0)
						{
							i2 = 1;
							st.giveItems(57, 10000);
						}
						else if(i0 == 1)
						{
							if(Rnd.get(1000) < 848)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 43)
								{
									st.giveItems(1884, 42);
								}
								else if(i1 < 66)
								{
									st.giveItems(1895, 36);
								}
								else if(i1 < 184)
								{
									st.giveItems(1876, 4);
								}
								else if(i1 < 250)
								{
									st.giveItems(1881, 6);
								}
								else if(i1 < 287)
								{
									st.giveItems(5549, 8);
								}
								else if(i1 < 484)
								{
									st.giveItems(1874, 1);
								}
								else if(i1 < 681)
								{
									st.giveItems(1889, 1);
								}
								else if(i1 < 799)
								{
									st.giveItems(1877, 1);
								}
								else if(i1 < 902)
								{
									st.giveItems(1894, 1);
								}
								else
								{
									st.giveItems(4043, 1);
								}
							}
							if(Rnd.get(1000) < 323)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 335)
								{
									st.giveItems(1888, 1);
								}
								else if(i1 < 556)
								{
									st.giveItems(4040, 1);
								}
								else if(i1 < 725)
								{
									st.giveItems(1890, 1);
								}
								else if(i1 < 872)
								{
									st.giveItems(5550, 1);
								}
								else if(i1 < 962)
								{
									st.giveItems(1893, 1);
								}
								else if(i1 < 986)
								{
									st.giveItems(4046, 1);
								}
								else
								{
									st.giveItems(4048, 1);
								}
							}
						}
						else if(i0 == 2)
						{
							if(Rnd.get(1000) < 847)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 148)
								{
									st.giveItems(1878, 8);
								}
								else if(i1 < 175)
								{
									st.giveItems(1882, 24);
								}
								else if(i1 < 273)
								{
									st.giveItems(1879, 4);
								}
								else if(i1 < 322)
								{
									st.giveItems(1880, 6);
								}
								else if(i1 < 357)
								{
									st.giveItems(1885, 6);
								}
								else if(i1 < 554)
								{
									st.giveItems(1875, 1);
								}
								else if(i1 < 685)
								{
									st.giveItems(1883, 1);
								}
								else if(i1 < 803)
								{
									st.giveItems(5220, 1);
								}
								else if(i1 < 901)
								{
									st.giveItems(4039, 1);
								}
								else
								{
									st.giveItems(4044, 1);
								}
							}
							if(Rnd.get(1000) < 251)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 350)
								{
									st.giveItems(1887, 1);
								}
								else if(i1 < 587)
								{
									st.giveItems(4042, 1);
								}
								else if(i1 < 798)
								{
									st.giveItems(1886, 1);
								}
								else if(i1 < 922)
								{
									st.giveItems(4041, 1);
								}
								else if(i1 < 966)
								{
									st.giveItems(1892, 1);
								}
								else if(i1 < 996)
								{
									st.giveItems(1891, 1);
								}
								else
								{
									st.giveItems(4047, 1);
								}
							}
						}
						else if(i0 == 3)
						{
							if(Rnd.get(1000) < 31)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 223)
								{
									st.giveItems(730, 1);
								}
								else if(i1 < 893)
								{
									st.giveItems(948, 1);
								}
								else
								{
									st.giveItems(960, 1);
								}
							}
							if(Rnd.get(1000) < 5)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 202)
								{
									st.giveItems(729, 1);
								}
								else if(i1 < 928)
								{
									st.giveItems(947, 1);
								}
								else
								{
									st.giveItems(959, 1);
								}
							}
						}
						else if(i0 == 4)
						{
							if(Rnd.get(1000) < 329)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 88)
								{
									st.giveItems(6698, 1);
								}
								else if(i1 < 185)
								{
									st.giveItems(6699, 1);
								}
								else if(i1 < 238)
								{
									st.giveItems(6700, 1);
								}
								else if(i1 < 262)
								{
									st.giveItems(6701, 1);
								}
								else if(i1 < 292)
								{
									st.giveItems(6702, 1);
								}
								else if(i1 < 356)
								{
									st.giveItems(6703, 1);
								}
								else if(i1 < 420)
								{
									st.giveItems(6704, 1);
								}
								else if(i1 < 482)
								{
									st.giveItems(6705, 1);
								}
								else if(i1 < 554)
								{
									st.giveItems(6706, 1);
								}
								else if(i1 < 576)
								{
									st.giveItems(6707, 1);
								}
								else if(i1 < 640)
								{
									st.giveItems(6708, 1);
								}
								else if(i1 < 704)
								{
									st.giveItems(6709, 1);
								}
								else if(i1 < 777)
								{
									st.giveItems(6710, 1);
								}
								else if(i1 < 799)
								{
									st.giveItems(6711, 1);
								}
								else if(i1 < 863)
								{
									st.giveItems(6712, 1);
								}
								else if(i1 < 927)
								{
									st.giveItems(6713, 1);
								}
								else
								{
									st.giveItems(6714, 1);
								}
							}
							if(Rnd.get(1000) < 54)
							{
								i2 = 1;
								int i1 = Rnd.get(1000);
								if(i1 < 100)
								{
									st.giveItems(6688, 1);
								}
								else if(i1 < 198)
								{
									st.giveItems(6689, 1);
								}
								else if(i1 < 298)
								{
									st.giveItems(6690, 1);
								}
								else if(i1 < 398)
								{
									st.giveItems(6691, 1);
								}
								else if(i1 < 499)
								{
									st.giveItems(7579, 1);
								}
								else if(i1 < 601)
								{
									st.giveItems(6693, 1);
								}
								else if(i1 < 703)
								{
									st.giveItems(6694, 1);
								}
								else if(i1 < 801)
								{
									st.giveItems(6695, 1);
								}
								else if(i1 < 902)
								{
									st.giveItems(6696, 1);
								}
								else
								{
									st.giveItems(6697, 1);
								}
							}
						}
						st.takeItems(7255, 1);
						if(i2 == 1)
						{
							return "el_lord_chamber_ghost_q0620_03.htm";
						}
						else if(i2 == 0)
						{
							return "el_lord_chamber_ghost_q0620_04.htm";
						}
					}
					else
					{
						st.takeItems(7255, 1);
						return "el_lord_chamber_ghost_q0620_05.htm";
					}
				}
				else if(player.getItemsCount(7255) == 0)
				{
					return "el_lord_chamber_ghost_q0620_06.htm";
				}
			}
			else if(reply == 101)
			{
				if(player.getItemsCount(7261) != 0)
				{
					st.takeItems(7261, 1);
					player.teleToLocation(178127, -84435, -7215);
				}
				else if(player.getItemsCount(7262) != 0)
				{
					player.teleToLocation(178127, -84435, -7215);
				}
				else
				{
					return "el_lord_chamber_ghost002.htm";
				}
			}
			else if(reply == 102)
			{
				if(player.getItemsCount(7261) != 0)
				{
					st.takeItems(7261, 1);
					player.teleToLocation(178127, -84435, -7215);
				}
				else if(player.getItemsCount(7262) != 0)
				{
					player.teleToLocation(178127, -84435, -7215);
				}
				else
				{
					return "el_lord_chamber_ghost002.htm";
				}
			}
		}
		else if(npc.getNpcId() == GHOST_CHAMBERLAIN_2)
		{
			if(st.isStarted())
			{
				switch(reply)
				{
					case 101:
						if(player.getItemsCount(7261) != 0)
						{
							st.takeItems(7261, 1);
							player.teleToLocation(178127, -84435, -7215);
						}
						else if(player.getItemsCount(7262) != 0)
						{
							player.teleToLocation(178127, -84435, -7215);
						}
						else
						{
							return "el_chamber_ghost002.htm";
						}
						break;
					case 102:
						if(player.getItemsCount(7261) != 0)
						{
							st.takeItems(7261, 1);
							player.teleToLocation(186699, -75915, -2826);
						}
						else if(player.getItemsCount(7262) != 0)
						{
							player.teleToLocation(186699, -75915, -2826);
						}
						else
						{
							return "el_chamber_ghost002.htm";
						}
						break;
				}
			}
		}
		else if(ArrayUtils.contains(LOCKS, npc.getNpcId()))
		{
			if(reply == 1)
			{
				switch(npc.getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						FourSepulchersManager.getInstance().spawnShadow(npc.getNpcId());
					default:
						if(player.getItemsCount(7260) == 0)
						{
							return npc.getServerName() + "002.htm";
						}
						else
						{
							st.takeItems(7260, 1);
							npc.openMyDoors(15000);
							ThreadPoolManager.getInstance().scheduleEffect(new SpawnNextMysteriousBox(npc.getNpcId()), 0);
						}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == NAMELESS_SPIRIT)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 74 ? "printessa_spirit_q0620_01.htm" : "printessa_spirit_q0620_12.htm";
				case STARTED:
					if(player.getItemsCount(7262) == 0 && (player.getItemsCount(7256) == 0 || player.getItemsCount(7257) == 0 || player.getItemsCount(7258) == 0 || player.getItemsCount(7259) == 0))
					{
						return "printessa_spirit_q0620_14.htm";
					}
					else if(player.getItemsCount(7262) == 0 && player.getItemsCount(7256) >= 1 && player.getItemsCount(7257) >= 1 && player.getItemsCount(7258) >= 1 && player.getItemsCount(7259) >= 1)
					{
						return "printessa_spirit_q0620_15.htm";
					}
					else if(player.getItemsCount(7262) >= 1)
					{
						return "printessa_spirit_q0620_17.htm";
					}
			}
		}
		else if(npc.getNpcId() == GHOST_OF_WIGOTH_1)
		{
			if(st.isStarted())
			{
				if(player.getItemsCount(7262) == 0 && (player.getItemsCount(7256) == 0 || player.getItemsCount(7257) == 0 || player.getItemsCount(7258) == 0 || player.getItemsCount(7259) == 0) && player.getItemsCount(7256) + player.getItemsCount(7257) + player.getItemsCount(7258) + player.getItemsCount(7259) < 3)
				{
					return "wigoth_ghost_a_q0620_01.htm";
				}
				else if(player.getItemsCount(7262) == 0 && (player.getItemsCount(7256) == 0 || player.getItemsCount(7257) == 0 || player.getItemsCount(7258) == 0 || player.getItemsCount(7259) == 0) && player.getItemsCount(7256) + player.getItemsCount(7257) + player.getItemsCount(7258) + player.getItemsCount(7259) >= 3)
				{
					return "wigoth_ghost_a_q0620_02.htm";
				}
				else if(player.getItemsCount(7262) == 0 && player.getItemsCount(7256) >= 1 && player.getItemsCount(7257) >= 1 && player.getItemsCount(7258) >= 1 && player.getItemsCount(7259) >= 1)
				{
					return "wigoth_ghost_a_q0620_04.htm";
				}
				else if(player.getItemsCount(7262) >= 1)
				{
					return "wigoth_ghost_a_q0620_05.htm";
				}
			}
		}
		else if(npc.getNpcId() == GHOST_OF_WIGOTH_2)
		{
			if(st.isStarted())
			{
				if(st.getMemoStateEx(1) == 2 && player.getItemsCount(7256) >= 1 && player.getItemsCount(7257) >= 1 && player.getItemsCount(7258) >= 1 && player.getItemsCount(7259) >= 1)
				{
					if(player.getItemsCount(7255) == 0 && player.getItemsCount(7254) < 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_01.htm";
					}
					if(player.getItemsCount(7255) >= 1 && player.getItemsCount(7254) < 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_02.htm";
					}
					if(player.getItemsCount(7255) == 0 && player.getItemsCount(7254) >= 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_03.htm";
					}
					if(player.getItemsCount(7255) >= 1 && player.getItemsCount(7254) >= 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_04.htm";
					}
				}
				else if(st.getMemoStateEx(1) == 2 && (player.getItemsCount(7256) == 0 || player.getItemsCount(7257) == 0 || player.getItemsCount(7258) == 0 || player.getItemsCount(7259) == 0))
				{
					if(player.getItemsCount(7255) == 0 && player.getItemsCount(7254) < 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_05.htm";
					}
					if(player.getItemsCount(7255) >= 1 && player.getItemsCount(7254) < 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_06.htm";
					}
					if(player.getItemsCount(7255) == 0 && player.getItemsCount(7254) >= 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_07.htm";
					}
					if(player.getItemsCount(7255) >= 1 && player.getItemsCount(7254) >= 1000)
					{
						st.setMemoStateEx(1, 3);
						return "wigoth_ghost_b_q0620_08.htm";
					}
				}
				else if(st.getMemoStateEx(1) == 3)
				{
					if(player.getItemsCount(7255) == 0 && player.getItemsCount(7254) < 1000)
					{
						return "wigoth_ghost_b_q0620_09.htm";
					}
					if(player.getItemsCount(7255) >= 1 && player.getItemsCount(7254) < 1000)
					{
						return "wigoth_ghost_b_q0620_10.htm";
					}
					if(player.getItemsCount(7255) == 0 && player.getItemsCount(7254) >= 1000)
					{
						return "wigoth_ghost_b_q0620_11.htm";
					}
					if(player.getItemsCount(7255) >= 1 && player.getItemsCount(7254) >= 1000)
					{
						return "wigoth_ghost_b_q0620_12.htm";
					}
				}
			}
		}
		else if(npc.getNpcId() == CONQ_SM || npc.getNpcId() == EMPER_SM || npc.getNpcId() == SAGES_SM || npc.getNpcId() == JUDGE_SM)
		{
			if(st.isStarted())
			{
				return npc.getServerName() + "_q0620_01.htm";
			}
		}
		else if(npc.getNpcId() == GHOST_CHAMBERLAIN_1)
		{
			if(st.isStarted())
			{
				return "el_lord_chamber_ghost_q0620_01.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(ArrayUtils.contains(TRIGGER_BOXES, npc.getNpcId()))
		{
			if(!npc.isDead())
			{
				npc.setIsInvul(false);
				npc.doDie(player);
				ThreadPoolManager.getInstance().scheduleAi(new SpawnMonsterForTriggerBox(npc.getNpcId()), 3500);
			}
		}
		else if(ArrayUtils.contains(KEY_BOXES, npc.getNpcId()))
		{
			if(!npc.isDead())
			{
				player.addItem(ProcessType.QUEST, 7260, 1, npc, true);
				npc.setIsInvul(false);
				npc.doDie(player);
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 74;
	}

	private static class SpawnMonsterForTriggerBox implements Runnable
	{
		private int _NpcId;

		public SpawnMonsterForTriggerBox(int npcId)
		{
			_NpcId = npcId;
		}

		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMonsterForTriggerBox(_NpcId);
		}
	}

	private static class SpawnNextMysteriousBox implements Runnable
	{
		private int _npcId;

		public SpawnNextMysteriousBox(int npcId)
		{
			_npcId = npcId;
		}

		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMysteriousBox(_npcId);
		}
	}
}