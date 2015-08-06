package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10366_RuinsStatusUpdate extends Quest
{
	private static final int SEBION = 32978;

	private static final int FRANCO = 32153;
	private static final int RIVIAN = 32147;
	private static final int DEVON = 32160;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int VALFAR = 32146;

	public _10366_RuinsStatusUpdate()
	{
		addStartNpc(SEBION);
		addTalkId(SEBION, RIVIAN, DEVON, TOOK, MOKA, VALFAR, FRANCO);
	}

	public static void main(String[] args)
	{
		new _10366_RuinsStatusUpdate();
	}

	@Override
	public int getQuestId()
	{
		return 10366;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			switch(qs.getPlayer().getRace())
			{
				case Human:
					qs.setCond(2);
					return "si_illusion_sebion_q10366_05.htm";
				case Elf:
					qs.setCond(3);
					return "si_illusion_sebion_q10366_06.htm";
				case DarkElf:
					qs.setCond(4);
					return "si_illusion_sebion_q10366_07.htm";
				case Orc:
					qs.setCond(5);
					return "si_illusion_sebion_q10366_08.htm";
				case Dwarf:
					qs.setCond(6);
					return "si_illusion_sebion_q10366_09.htm";
				case Kamael:
					qs.setCond(7);
					return "si_illusion_sebion_q10366_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == SEBION)
		{
			if(reply == 1)
			{
				return "si_illusion_sebion_q10366_04.htm";
			}
		}
		else
		{
			if(reply == 2)
			{
				return npc.getServerName() + "_q10366_02.htm";
			}
			else if(reply == 3)
			{
				st.giveAdena(75000, true);
				st.addExpAndSp(150000, 36);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return npc.getServerName() + "_q10366_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == SEBION)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_sebion_q10366_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_sebion_q10366_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32978-2.html";
					}
					break;
				case COMPLETED:
					return "si_illusion_sebion_q10366_03.htm";
			}
		}
		else
		{
			if(st.getCond() == 2 && npc.getNpcId() == FRANCO ||
				st.getCond() == 3 && npc.getNpcId() == RIVIAN ||
				st.getCond() == 4 && npc.getNpcId() == DEVON ||
				st.getCond() == 5 && npc.getNpcId() == TOOK ||
				st.getCond() == 6 && npc.getNpcId() == MOKA ||
				st.getCond() == 7 && npc.getNpcId() == VALFAR)
			{
				return npc.getServerName() + "_q10366_01.htm";
			}
			else
			{
				return st.isCompleted() ? npc.getServerName() + "_q10366_03.htm" : npc.getServerName() + "_q10366_05.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10365_SeekerEscort.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 16 && player.getLevel() <= 25;

	}
} 