package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.12.12
 * Time: 6:00
 */

public class _00372_LegacyOfInsolence extends Quest
{
	// Квестовые персонажи
	private static int HOLLY = 30839;
	private static int WALDERAL = 30844;
	private static int DESMOND = 30855;
	private static int PATRIN = 30929;
	private static int CLAUDIA = 31001;

	// Квестовые монстры
	private static int CORRUPT_SAGE = 20817;
	private static int ERIN_EDIUNCE = 20821;
	private static int HALLATE_INSP = 20825;
	private static int PLATINUM_OVL = 20829;
	private static int PLATINUM_PRE = 21069;
	private static int MESSENGER_A1 = 21062;
	private static int MESSENGER_A2 = 21063;

	// Квестовые предметы
	private static int Ancient_Red_Papyrus = 5966;
	private static int Ancient_Blue_Papyrus = 5967;
	private static int Ancient_Black_Papyrus = 5968;
	private static int Ancient_White_Papyrus = 5969;

	private final Map<Integer, int[]> DROPLIST = new HashMap<>();

	public _00372_LegacyOfInsolence()
	{
		addStartNpc(WALDERAL);
		addTalkId(HOLLY, DESMOND, PATRIN, CLAUDIA);
		addKillId(CORRUPT_SAGE, ERIN_EDIUNCE, HALLATE_INSP, PLATINUM_OVL, PLATINUM_PRE, MESSENGER_A1, MESSENGER_A2);

		DROPLIST.put(CORRUPT_SAGE, new int[]{
			Ancient_Red_Papyrus, 35
		});
		DROPLIST.put(ERIN_EDIUNCE, new int[]{
			Ancient_Red_Papyrus, 40
		});
		DROPLIST.put(HALLATE_INSP, new int[]{
			Ancient_Red_Papyrus, 45
		});
		DROPLIST.put(PLATINUM_OVL, new int[]{
			Ancient_Blue_Papyrus, 40
		});
		DROPLIST.put(PLATINUM_PRE, new int[]{
			Ancient_Black_Papyrus, 25
		});
		DROPLIST.put(MESSENGER_A1, new int[]{
			Ancient_White_Papyrus, 25
		});
		DROPLIST.put(MESSENGER_A2, new int[]{
			Ancient_White_Papyrus, 25
		});
	}

	public static void main(String[] args)
	{
		new _00372_LegacyOfInsolence();
	}

	@Override
	public int getQuestId()
	{
		return 372;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "whouse_keeper_walderal_q0372_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == WALDERAL)
		{
			if(reply == 1)
			{
				return "whouse_keeper_walderal_q0372_03.htm";
			}
			if(reply == 3)
			{
				if(player.getItemsCount(5989) < 1 || player.getItemsCount(5990) < 1 || player.getItemsCount(5991) < 1 || player.getItemsCount(5992) < 1 || player.getItemsCount(5993) < 1 || player.getItemsCount(5994) < 1 || player.getItemsCount(5995) < 1 || player.getItemsCount(5996) < 1 || player.getItemsCount(5997) < 1 || player.getItemsCount(5998) < 1 || player.getItemsCount(5999) < 1 || player.getItemsCount(6000) < 1 || player.getItemsCount(6001) < 1)
				{
					return "whouse_keeper_walderal_q0372_06.htm";
				}
				else if(player.getItemsCount(5989) >= 1 && player.getItemsCount(5990) >= 1 && player.getItemsCount(5991) >= 1 && player.getItemsCount(5992) >= 1 && player.getItemsCount(5993) >= 1 && player.getItemsCount(5994) >= 1 && player.getItemsCount(5995) >= 1 && player.getItemsCount(5996) >= 1 && player.getItemsCount(5997) >= 1 && player.getItemsCount(5998) >= 1 && player.getItemsCount(5999) >= 1 && player.getItemsCount(6000) >= 1 && player.getItemsCount(6001) >= 1)
				{
					return "whouse_keeper_walderal_q0372_07.htm";
				}
			}
			if(reply == 4)
			{
				return "whouse_keeper_walderal_q0372_08.htm";
			}
			if(reply == 5)
			{
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "whouse_keeper_walderal_q0372_09.htm";
			}
			if(reply == 6)
			{
				return "whouse_keeper_walderal_q0372_11.htm";
			}
			if(reply == 7)
			{
				if(player.getItemsCount(5989) < 1 || player.getItemsCount(5990) < 1 || player.getItemsCount(5991) < 1 || player.getItemsCount(5992) < 1 || player.getItemsCount(5993) < 1 || player.getItemsCount(5994) < 1 || player.getItemsCount(5995) < 1 || player.getItemsCount(5996) < 1 || player.getItemsCount(5997) < 1 || player.getItemsCount(5998) < 1 || player.getItemsCount(5999) < 1 || player.getItemsCount(6000) < 1 || player.getItemsCount(6001) < 1)
				{
					return "whouse_keeper_walderal_q0372_07e.htm";
				}
				else if(player.getItemsCount(5989) >= 1 && player.getItemsCount(5990) >= 1 && player.getItemsCount(5991) >= 1 && player.getItemsCount(5992) >= 1 && player.getItemsCount(5993) >= 1 && player.getItemsCount(5994) >= 1 && player.getItemsCount(5995) >= 1 && player.getItemsCount(5996) >= 1 && player.getItemsCount(5997) >= 1 && player.getItemsCount(5998) >= 1 && player.getItemsCount(5999) >= 1 && player.getItemsCount(6000) >= 1 && player.getItemsCount(6001) >= 1)
				{
					st.takeItems(5989, 1);
					st.takeItems(5990, 1);
					st.takeItems(5991, 1);
					st.takeItems(5992, 1);
					st.takeItems(5993, 1);
					st.takeItems(5994, 1);
					st.takeItems(5995, 1);
					st.takeItems(5996, 1);
					st.takeItems(5997, 1);
					st.takeItems(5998, 1);
					st.takeItems(5999, 1);
					st.takeItems(6000, 1);
					st.takeItems(6001, 1);
					int i1 = Rnd.get(100);
					if(i1 < 10)
					{
						st.giveItems(5496, 1);
					}
					else if(i1 < 20)
					{
						st.giveItems(5508, 1);
					}
					else if(i1 < 30)
					{
						st.giveItems(5525, 1);
					}
					else if(i1 < 40)
					{
						st.giveItems(5496, 1);
						st.giveItems(5508, 1);
						st.giveItems(5525, 1);
					}
					else if(i1 < 51)
					{
						st.giveItems(5368, 1);
					}
					else if(i1 < 62)
					{
						st.giveItems(5392, 1);
					}
					else if(i1 < 79)
					{
						st.giveItems(5426, 1);
					}
					else if(i1 < 100)
					{
						st.giveItems(5368, 1);
						st.giveItems(5392, 1);
						st.giveItems(5426, 1);
					}
					return "whouse_keeper_walderal_q0372_07a.htm";
				}
			}
			if(reply == 8)
			{
				if(player.getItemsCount(5989) < 1 || player.getItemsCount(5990) < 1 || player.getItemsCount(5991) < 1 || player.getItemsCount(5992) < 1 || player.getItemsCount(5993) < 1 || player.getItemsCount(5994) < 1 || player.getItemsCount(5995) < 1 || player.getItemsCount(5996) < 1 || player.getItemsCount(5997) < 1 || player.getItemsCount(5998) < 1 || player.getItemsCount(5999) < 1 || player.getItemsCount(6000) < 1 || player.getItemsCount(6001) < 1)
				{
					return "whouse_keeper_walderal_q0372_07e.htm";
				}
				else if(player.getItemsCount(5989) >= 1 && player.getItemsCount(5990) >= 1 && player.getItemsCount(5991) >= 1 && player.getItemsCount(5992) >= 1 && player.getItemsCount(5993) >= 1 && player.getItemsCount(5994) >= 1 && player.getItemsCount(5995) >= 1 && player.getItemsCount(5996) >= 1 && player.getItemsCount(5997) >= 1 && player.getItemsCount(5998) >= 1 && player.getItemsCount(5999) >= 1 && player.getItemsCount(6000) >= 1 && player.getItemsCount(6001) >= 1)
				{
					st.takeItems(5989, 1);
					st.takeItems(5990, 1);
					st.takeItems(5991, 1);
					st.takeItems(5992, 1);
					st.takeItems(5993, 1);
					st.takeItems(5994, 1);
					st.takeItems(5995, 1);
					st.takeItems(5996, 1);
					st.takeItems(5997, 1);
					st.takeItems(5998, 1);
					st.takeItems(5999, 1);
					st.takeItems(6000, 1);
					st.takeItems(6001, 1);
					int i1 = Rnd.get(100);
					if(i1 < 10)
					{
						st.giveItems(5497, 1);
					}
					else if(i1 < 20)
					{
						st.giveItems(5509, 1);
					}
					else if(i1 < 30)
					{
						st.giveItems(5526, 1);
					}
					else if(i1 < 40)
					{
						st.giveItems(5497, 1);
						st.giveItems(5509, 1);
						st.giveItems(5526, 1);
					}
					else if(i1 < 51)
					{
						st.giveItems(5370, 1);
					}
					else if(i1 < 62)
					{
						st.giveItems(5394, 1);
					}
					else if(i1 < 79)
					{
						st.giveItems(5428, 1);
					}
					else if(i1 < 100)
					{
						st.giveItems(5370, 1);
						st.giveItems(5394, 1);
						st.giveItems(5428, 1);
					}
					return "whouse_keeper_walderal_q0372_07b.htm";
				}
			}
			if(reply == 9)
			{
				if(player.getItemsCount(5989) < 1 || player.getItemsCount(5990) < 1 || player.getItemsCount(5991) < 1 || player.getItemsCount(5992) < 1 || player.getItemsCount(5993) < 1 || player.getItemsCount(5994) < 1 || player.getItemsCount(5995) < 1 || player.getItemsCount(5996) < 1 || player.getItemsCount(5997) < 1 || player.getItemsCount(5998) < 1 || player.getItemsCount(5999) < 1 || player.getItemsCount(6000) < 1 || player.getItemsCount(6001) < 1)
				{
					return "whouse_keeper_walderal_q0372_07e.htm";
				}
				else if(player.getItemsCount(5989) >= 1 && player.getItemsCount(5990) >= 1 && player.getItemsCount(5991) >= 1 && player.getItemsCount(5992) >= 1 && player.getItemsCount(5993) >= 1 && player.getItemsCount(5994) >= 1 && player.getItemsCount(5995) >= 1 && player.getItemsCount(5996) >= 1 && player.getItemsCount(5997) >= 1 && player.getItemsCount(5998) >= 1 && player.getItemsCount(5999) >= 1 && player.getItemsCount(6000) >= 1 && player.getItemsCount(6001) >= 1)
				{
					st.takeItems(5989, 1);
					st.takeItems(5990, 1);
					st.takeItems(5991, 1);
					st.takeItems(5992, 1);
					st.takeItems(5993, 1);
					st.takeItems(5994, 1);
					st.takeItems(5995, 1);
					st.takeItems(5996, 1);
					st.takeItems(5997, 1);
					st.takeItems(5998, 1);
					st.takeItems(5999, 1);
					st.takeItems(6000, 1);
					st.takeItems(6001, 1);
					int i1 = Rnd.get(100);
					if(i1 < 17)
					{
						st.giveItems(5502, 1);
					}
					else if(i1 < 34)
					{
						st.giveItems(5514, 1);
					}
					else if(i1 < 49)
					{
						st.giveItems(5527, 1);
					}
					else if(i1 < 58)
					{
						st.giveItems(5502, 1);
						st.giveItems(5514, 1);
						st.giveItems(5527, 1);
					}
					else if(i1 < 70)
					{
						st.giveItems(5380, 1);
					}
					else if(i1 < 82)
					{
						st.giveItems(5404, 1);
					}
					else if(i1 < 92)
					{
						st.giveItems(5430, 1);
					}
					else if(i1 < 100)
					{
						st.giveItems(5380, 1);
						st.giveItems(5404, 1);
						st.giveItems(5430, 1);
					}
					return "whouse_keeper_walderal_q0372_07c.htm";
				}
			}
			if(reply == 10)
			{
				if(player.getItemsCount(5989) < 1 || player.getItemsCount(5990) < 1 || player.getItemsCount(5991) < 1 || player.getItemsCount(5992) < 1 || player.getItemsCount(5993) < 1 || player.getItemsCount(5994) < 1 || player.getItemsCount(5995) < 1 || player.getItemsCount(5996) < 1 || player.getItemsCount(5997) < 1 || player.getItemsCount(5998) < 1 || player.getItemsCount(5999) < 1 || player.getItemsCount(6000) < 1 || player.getItemsCount(6001) < 1)
				{
					return "whouse_keeper_walderal_q0372_07e.htm";
				}
				else if(player.getItemsCount(5989) >= 1 && player.getItemsCount(5990) >= 1 && player.getItemsCount(5991) >= 1 && player.getItemsCount(5992) >= 1 && player.getItemsCount(5993) >= 1 && player.getItemsCount(5994) >= 1 && player.getItemsCount(5995) >= 1 && player.getItemsCount(5996) >= 1 && player.getItemsCount(5997) >= 1 && player.getItemsCount(5998) >= 1 && player.getItemsCount(5999) >= 1 && player.getItemsCount(6000) >= 1 && player.getItemsCount(6001) >= 1)
				{
					st.takeItems(5989, 1);
					st.takeItems(5990, 1);
					st.takeItems(5991, 1);
					st.takeItems(5992, 1);
					st.takeItems(5993, 1);
					st.takeItems(5994, 1);
					st.takeItems(5995, 1);
					st.takeItems(5996, 1);
					st.takeItems(5997, 1);
					st.takeItems(5998, 1);
					st.takeItems(5999, 1);
					st.takeItems(6000, 1);
					st.takeItems(6001, 1);
					int i1 = Rnd.get(100);
					if(i1 < 17)
					{
						st.giveItems(5503, 1);
					}
					else if(i1 < 34)
					{
						st.giveItems(5515, 1);
					}
					else if(i1 < 49)
					{
						st.giveItems(5528, 1);
					}
					else if(i1 < 58)
					{
						st.giveItems(5503, 1);
						st.giveItems(5515, 1);
						st.giveItems(5528, 1);
					}
					else if(i1 < 70)
					{
						st.giveItems(5382, 1);
					}
					else if(i1 < 82)
					{
						st.giveItems(5406, 1);
					}
					else if(i1 < 92)
					{
						st.giveItems(5432, 1);
					}
					else if(i1 < 100)
					{
						st.giveItems(5382, 1);
						st.giveItems(5406, 1);
						st.giveItems(5432, 1);
					}
					return "whouse_keeper_walderal_q0372_07d.htm";
				}
			}
			if(reply == 99)
			{
				st.setCond(2);
				return "whouse_keeper_walderal_q0372_05b.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2Party party = st.getPlayer().getParty();
		if(party != null)
		{
			st = party.getRandomPartyMember().getQuestState(getClass());
		}

		if(st != null && st.isStarted())
		{
			int[] dropData = DROPLIST.get(npc.getNpcId());
			if(Rnd.getChance(dropData[1]))
			{
				st.giveItem(dropData[0]);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == 1)
		{
			switch(st.getState())
			{
				case CREATED:
					if(npc.getNpcId() == WALDERAL)
					{
						if(player.getLevel() < 59)
						{
							return "whouse_keeper_walderal_q0372_01.htm";
						}
						else if(player.getLevel() >= 59)
						{
							return "whouse_keeper_walderal_q0372_02.htm";
						}
					}
				case STARTED:
					if(npc.getNpcId() == WALDERAL)
					{
						return "whouse_keeper_walderal_q0372_05.htm";
					}
					else if(npc.getNpcId() == HOLLY)
					{
						if(player.getItemsCount(5984) < 1 || player.getItemsCount(5985) < 1 || player.getItemsCount(5986) < 1 || player.getItemsCount(5987) < 1 || player.getItemsCount(5988) < 1)
						{
							return "trader_holly_q0372_01.htm";
						}
						else if(player.getItemsCount(5984) >= 1 && player.getItemsCount(5985) >= 1 && player.getItemsCount(5986) >= 1 && player.getItemsCount(5987) >= 1 && player.getItemsCount(5988) >= 1)
						{
							st.takeItems(5984, 1);
							st.takeItems(5985, 1);
							st.takeItems(5986, 1);
							st.takeItems(5987, 1);
							st.takeItems(5988, 1);
							int i1 = Rnd.get(100);
							if(i1 < 30)
							{
								st.giveItems(5496, 1);
							}
							else if(i1 < 60)
							{
								st.giveItems(5508, 1);
							}
							else if(i1 < 80)
							{
								st.giveItems(5525, 1);
							}
							else if(i1 < 90)
							{
								st.giveItems(5496, 1);
								st.giveItems(5508, 1);
								st.giveItems(5525, 1);
							}
							else if(i1 < 100)
							{
								st.giveAdena(4000, true);
							}
							return "trader_holly_q0372_02.htm";
						}
					}
					else if(npc.getNpcId() == CLAUDIA)
					{
						if(player.getItemsCount(5972) < 1 || player.getItemsCount(5973) < 1 || player.getItemsCount(5974) < 1 || player.getItemsCount(5975) < 1 || player.getItemsCount(5976) < 1 || player.getItemsCount(5977) < 1 || player.getItemsCount(5978) < 1)
						{
							return "claudia_a_q0372_01.htm";
						}
						else if(player.getItemsCount(5972) >= 1 && player.getItemsCount(5973) >= 1 && player.getItemsCount(5974) >= 1 && player.getItemsCount(5975) >= 1 && player.getItemsCount(5976) >= 1 && player.getItemsCount(5977) >= 1 && player.getItemsCount(5978) >= 1)
						{
							st.takeItems(5972, 1);
							st.takeItems(5973, 1);
							st.takeItems(5974, 1);
							st.takeItems(5975, 1);
							st.takeItems(5976, 1);
							st.takeItems(5977, 1);
							st.takeItems(5978, 1);
							int i1 = Rnd.get(100);
							if(i1 < 31)
							{
								st.giveItems(5502, 1);
							}
							else if(i1 < 62)
							{
								st.giveItems(5514, 1);
							}
							else if(i1 < 75)
							{
								st.giveItems(5527, 1);
							}
							else if(i1 < 83)
							{
								st.giveItems(5502, 1);
								st.giveItems(5514, 1);
								st.giveItems(5527, 1);
							}
							else if(i1 < 100)
							{
								st.giveAdena(4000, true);
							}
							return "claudia_a_q0372_02.htm";
						}
					}
					else if(npc.getNpcId() == PATRIN)
					{
						if(player.getItemsCount(5979) < 1 || player.getItemsCount(5980) < 1 || player.getItemsCount(5981) < 1 || player.getItemsCount(5982) < 1 || player.getItemsCount(5983) < 1)
						{
							return "patrin_q0372_01.htm";
						}
						else if(player.getItemsCount(5979) >= 1 && player.getItemsCount(5980) >= 1 && player.getItemsCount(5981) >= 1 && player.getItemsCount(5982) >= 1 && player.getItemsCount(5983) >= 1)
						{
							st.takeItems(5979, 1);
							st.takeItems(5980, 1);
							st.takeItems(5981, 1);
							st.takeItems(5982, 1);
							st.takeItems(5983, 1);
							int i1 = Rnd.get(100);
							if(i1 < 30)
							{
								st.giveItems(5497, 1);
							}
							else if(i1 < 60)
							{
								st.giveItems(5509, 1);
							}
							else if(i1 < 80)
							{
								st.giveItems(5526, 1);
							}
							else if(i1 < 90)
							{
								st.giveItems(5497, 1);
								st.giveItems(5509, 1);
								st.giveItems(5526, 1);
							}
							else if(i1 < 100)
							{
								st.giveAdena(4000, true);
							}
							return "patrin_q0372_02.htm";
						}
					}
					else if(npc.getNpcId() == DESMOND)
					{
						if(player.getItemsCount(5972) < 1 || player.getItemsCount(5973) < 1 || player.getItemsCount(5974) < 1 || player.getItemsCount(5975) < 1 || player.getItemsCount(5976) < 1 || player.getItemsCount(5977) < 1 || player.getItemsCount(5978) < 1)
						{
							return "magister_desmond_q0372_01.htm";
						}
						else if(player.getItemsCount(5972) >= 1 && player.getItemsCount(5973) >= 1 && player.getItemsCount(5974) >= 1 && player.getItemsCount(5975) >= 1 && player.getItemsCount(5976) >= 1 && player.getItemsCount(5977) >= 1 && player.getItemsCount(5978) >= 1)
						{
							st.takeItems(5972, 1);
							st.takeItems(5973, 1);
							st.takeItems(5974, 1);
							st.takeItems(5975, 1);
							st.takeItems(5976, 1);
							st.takeItems(5977, 1);
							st.takeItems(5978, 1);
							int i1 = Rnd.get(100);
							if(i1 < 31)
							{
								st.giveItems(5503, 1);
							}
							else if(i1 < 62)
							{
								st.giveItems(5515, 1);
							}
							else if(i1 < 75)
							{
								st.giveItems(5528, 1);
							}
							else if(i1 < 83)
							{
								st.giveItems(5503, 1);
								st.giveItems(5515, 1);
								st.giveItems(5528, 1);
							}
							else if(i1 < 100)
							{
								st.giveAdena(4000, true);
							}
							return "magister_desmond_q0372_02.htm";
						}
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 59;
	}
}