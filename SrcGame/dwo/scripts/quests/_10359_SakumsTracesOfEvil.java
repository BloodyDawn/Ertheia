package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _10359_SakumsTracesOfEvil extends Quest
{
	//npc
	private static final int ADVENTURE_GUILDSMAN = 31795;
	private static final int FRED = 33179;

	private static final int MENDIO = 30504;
	private static final int RAYMOND = 30289;
	private static final int RAINS = 30288;
	private static final int TOBIAS = 30297;
	private static final int DRIKUS = 30505;
	private static final int GERSHWIN = 32196;
	private static final int ELLENIA = 30155;
	private static final int ESRANDELL = 30158;
	// items
	private static final int SUSPICIOUS_FRAGMENT = 17586;

	private static final int[] MOBS = {20070, 20067, 20072, 23026, 23098, 23097};

	public _10359_SakumsTracesOfEvil()
	{
		addStartNpc(ADVENTURE_GUILDSMAN);
		addTalkId(ADVENTURE_GUILDSMAN, FRED, MENDIO, RAYMOND, RAINS, TOBIAS, DRIKUS, GERSHWIN, ELLENIA, ESRANDELL);
		addKillId(MOBS);
		questItemIds = new int[]{SUSPICIOUS_FRAGMENT};
	}

	public static void main(String[] args)
	{
		new _10359_SakumsTracesOfEvil();
	}

	@Override
	public int getQuestId()
	{
		return 10359;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("adventurer_agent_town_21_q10359_06.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("33179-03.html"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("finish"))
		{
			switch(player.getRace())
			{
				case Human:
					return player.isMageClass() ? "bishop_raimund_q10359_11.html" : "master_rains_q10359_11.html";
				case Elf:
					return player.isMageClass() ? "eso_q10359_11.html" : "elliasin_q10359_11.html";
				case DarkElf:
					return "master_tobias_q10359_11.html";
				case Orc:
					return "high_prefect_drikus_q10359_11.html";
				case Dwarf:
					return "head_blacksmith_mendio_q10359_11.html";
				case Kamael:
					return "grandmaster_gershuin_q10359_11.html";
			}
			st.giveAdena(108000, true);
			st.addExpAndSp(670000, 220000);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}
		if(st.getCond() == 2 && st.getQuestItemsCount(SUSPICIOUS_FRAGMENT) < 20 && Rnd.getChance(20))
		{
			st.giveItems(SUSPICIOUS_FRAGMENT, 1);
		}
		if(st.getCond() == 2 && st.getQuestItemsCount(SUSPICIOUS_FRAGMENT) >= 20)
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == ADVENTURE_GUILDSMAN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 34 && player.getLevel() < 40)
					{
						return "adventurer_agent_town_21_q10359_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "adventurer_agent_town_21_q10359_02.html";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "adventurer_agent_town_21_q10359_07.htm";
					}
					break;
				case COMPLETED:
					return "adventurer_agent_town_21_q10359_03.html";
			}
		}
		else if(npc.getNpcId() == FRED)
		{
			if(st.getCond() == 1)
			{
				return "33179-01.html";
			}
			else if(st.getCond() == 2)
			{
				return "33179-04.html";
			}
			else if(st.getCond() == 3)
			{
				switch(player.getRace())
				{
					case Human:
						if(player.isMageClass())
						{
							st.setCond(4);
						}
						else
						{
							st.setCond(5);
						}
						break;
					case Elf:
						if(player.isMageClass())
						{
							st.setCond(11);
						}
						else
						{
							st.setCond(10);
						}
						break;
					case DarkElf:
						st.setCond(6);
						break;
					case Orc:
						st.setCond(7);
						break;
					case Dwarf:
						st.setCond(8);
						break;
					case Kamael:
						st.setCond(9);
						break;
				}
				st.takeItems(SUSPICIOUS_FRAGMENT, -1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "33179-05.html";
			}
		}
		else if(npc.getNpcId() == MENDIO)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "head_blacksmith_mendio_q10359_05.html" : "head_blacksmith_mendio_q10359_06.html";
					case Elf:
						return player.isMageClass() ? "head_blacksmith_mendio_q10359_04.html" : "head_blacksmith_mendio_q10359_03.html";
					case DarkElf:
						return "head_blacksmith_mendio_q10359_07.html";
					case Orc:
						return "head_blacksmith_mendio_q10359_02.html";
					case Dwarf:
						return "head_blacksmith_mendio_q10359_01.html";
					case Kamael:
						return "head_blacksmith_mendio_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "head_blacksmith_mendio_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == RAYMOND)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "bishop_raimund_q10359_01.html" : "bishop_raimund_q10359_02.html";
					case Elf:
						return player.isMageClass() ? "bishop_raimund_q10359_04.html" : "bishop_raimund_q10359_03.html";
					case DarkElf:
						return "bishop_raimund_q10359_05.html";
					case Orc:
						return "bishop_raimund_q10359_06.html";
					case Dwarf:
						return "bishop_raimund_q10359_07.html";
					case Kamael:
						return "bishop_raimund_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "bishop_raimund_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == RAINS)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "master_rains_q10359_02.html" : "master_rains_q10359_01.html";
					case Elf:
						return player.isMageClass() ? "master_rains_q10359_04.html" : "master_rains_q10359_03.html";
					case DarkElf:
						return "master_rains_q10359_05.html";
					case Orc:
						return "master_rains_q10359_06.html";
					case Dwarf:
						return "master_rains_q10359_07.html";
					case Kamael:
						return "master_rains_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "master_rains_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == TOBIAS)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "master_tobias_q10359_04.html" : "master_tobias_q10359_05.html";
					case Elf:
						return player.isMageClass() ? "master_tobias_q10359_03.html" : "master_tobias_q10359_02.html";
					case DarkElf:
						return "master_tobias_q10359_01.html";
					case Orc:
						return "master_tobias_q10359_06.html";
					case Dwarf:
						return "master_tobias_q10359_07.html";
					case Kamael:
						return "master_tobias_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "master_tobias_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == DRIKUS)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "high_prefect_drikus_q10359_05.html" : "high_prefect_drikus_q10359_06.html";
					case Elf:
						return player.isMageClass() ? "high_prefect_drikus_q10359_04.html" : "high_prefect_drikus_q10359_03.html";
					case DarkElf:
						return "high_prefect_drikus_q10359_02.html";
					case Orc:
						return "high_prefect_drikus_q10359_01.html";
					case Dwarf:
						return "high_prefect_drikus_q10359_07.html";
					case Kamael:
						return "high_prefect_drikus_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "high_prefect_drikus_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == GERSHWIN)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "grandmaster_gershuin_q10359_06.html" : "grandmaster_gershuin_q10359_07.html";
					case Elf:
						return player.isMageClass() ? "grandmaster_gershuin_q10359_05.html" : "grandmaster_gershuin_q10359_04.html";
					case DarkElf:
						return "grandmaster_gershuin_q10359_02.html";
					case Orc:
						return "grandmaster_gershuin_q10359_03.html";
					case Dwarf:
						return "grandmaster_gershuin_q10359_08.html";
					case Kamael:
						return "grandmaster_gershuin_q10359_01.html";
				}
			}
			else if(st.isCompleted())
			{
				return "grandmaster_gershuin_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == ELLENIA)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "elliasin_q10359_05.html" : "elliasin_q10359_06.html";
					case Elf:
						return player.isMageClass() ? "elliasin_q10359_02.html" : "elliasin_q10359_01.html";
					case DarkElf:
						return "elliasin_q10359_03.html";
					case Orc:
						return "elliasin_q10359_04.html";
					case Dwarf:
						return "elliasin_q10359_07.html";
					case Kamael:
						return "elliasin_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "elliasin_q10359_09.html";
			}
		}
		else if(npc.getNpcId() == ESRANDELL)
		{
			if(st.getCond() > 3)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "eso_q10359_05.html" : "eso_q10359_06.html";
					case Elf:
						return player.isMageClass() ? "eso_q10359_01.html" : "eso_q10359_02.html";
					case DarkElf:
						return "eso_q10359_03.html";
					case Orc:
						return "eso_q10359_04.html";
					case Dwarf:
						return "eso_q10359_07.html";
					case Kamael:
						return "eso_q10359_08.html";
				}
			}
			else if(st.isCompleted())
			{
				return "eso_q10359_09.html";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 34 && player.getLevel() < 40;
	}
} 