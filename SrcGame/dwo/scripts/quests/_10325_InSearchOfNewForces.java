package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.11
 * Time: 22:58
 */

public class _10325_InSearchOfNewForces extends Quest
{
	// Квестовые персонажи
	private static final int Gallint = 32980;
	private static final int Talbot = 32156; // Human
	private static final int Sindet = 32148; // Elf
	private static final int Black = 32161; // Dark Elf
	private static final int Kankaid = 32159; // Dwarf
	private static final int Xonia = 32144; // Kamael
	private static final int Herz = 32151;  // Orc

	public _10325_InSearchOfNewForces()
	{
		addStartNpc(Gallint);
		addTalkId(Gallint, Sindet, Talbot, Black, Kankaid, Xonia, Herz);
	}

	public static void main(String[] args)
	{
		new _10325_InSearchOfNewForces();
	}

	@Override
	public int getQuestId()
	{
		return 10325;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.setState(QuestStateType.STARTED);
			qs.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
			switch(qs.getPlayer().getRace())
			{
				case Human:
					qs.setCond(2);
					return "si_galint_new_q10325_05.htm";
				case Elf:
					qs.setCond(3);
					return "si_galint_new_q10325_06.htm";
				case DarkElf:
					qs.setCond(4);
					return "si_galint_new_q10325_07.htm";
				case Orc:
					qs.setCond(5);
					return "si_galint_new_q10325_08.htm";
				case Dwarf:
					qs.setCond(6);
					return "si_galint_new_q10325_09.htm";
				case Kamael:
					qs.setCond(7);
					return "si_galint_new_q10325_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Gallint)
		{
			if(reply == 1)
			{
				return "si_galint_new_q10325_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prevSt = player.getQuestState(_10324_MeetingWithGallint.class);

		if(npc.getNpcId() == Gallint)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_galint_new_q10325_03.htm";
				case CREATED:
					if(player.getLevel() < 20 && prevSt != null && prevSt.isCompleted())
					{
						return "si_galint_new_q10325_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_galint_new_q10325_02.htm";
					}
				case STARTED:
					if(st.getCond() < 7)
					{
						return "si_galint_new_q10325_11.htm";
					}
					else
					{
						st.giveAdena(12000, true);
						if(player.isMageClass())
						{
							st.giveItems(2509, 1000);
						}
						else
						{
							st.giveItems(1835, 1000);
						}
						st.addExpAndSp(3254, 5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "si_galint_new_q10325_12.htm";
					}
			}
		}
		else if(npc.getNpcId() == Talbot)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(8);
					return "master_talbot_q10325_01.htm";
				}
				else
				{
					return st.getCond() == 8 ? "master_talbot_q10325_04.htm" : "master_talbot_q10325_02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "master_talbot_q10325_03.htm";
			}
		}
		else if(npc.getNpcId() == Sindet)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 3)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(9);
					return "master_cidnet_q10325_01.htm";
				}
				else
				{
					return st.getCond() == 9 ? "master_cidnet_q10325_04.htm" : "master_cidnet_q10325_02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "master_cidnet_q10325_03.htm";
			}
		}
		else if(npc.getNpcId() == Black)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 4)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(10);
					return "master_black_q10325_01.htm";
				}
				else
				{
					return st.getCond() == 10 ? "master_black_q10325_04.htm" : "master_black_q10325_02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "master_black_q10325_03.htm";
			}
		}
		else if(npc.getNpcId() == Herz)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 5)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(11);
					return "prefect_herz_q10325_01.htm";
				}
				else
				{
					return st.getCond() == 11 ? "prefect_herz_q10325_04.htm" : "prefect_herz_q10325_02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "prefect_herz_q10325_03.htm";
			}
		}
		else if(npc.getNpcId() == Kankaid)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 6)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(12);
					return "blacksmith_camcad_q10325_01.htm";
				}
				else
				{
					return st.getCond() == 12 ? "blacksmith_camcad_q10325_04.htm" : "blacksmith_camcad_q10325_02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "blacksmith_camcad_q10325_03.htm";
			}
		}
		else if(npc.getNpcId() == Xonia)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 7)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(13);
					return "master_xonia_q10325_01.htm";
				}
				else
				{
					return st.getCond() == 13 ? "master_xonia_q10325_04.htm" : "master_xonia_q10325_02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "master_xonia_q10325_03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10324_MeetingWithGallint.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;

	}
}
