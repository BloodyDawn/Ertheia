package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10361_RolesOfTheSeeker extends Quest
{
	private static final int LAKCIS = 32977;
	private static final int CHESHA = 33449;

	public _10361_RolesOfTheSeeker()
	{
		addStartNpc(LAKCIS);
		addTalkId(LAKCIS, CHESHA);
	}

	public static void main(String[] args)
	{
		new _10361_RolesOfTheSeeker();
	}

	@Override
	public int getQuestId()
	{
		return 10361;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_larcis_q10361_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == LAKCIS)
		{
			if(reply == 1)
			{
				return "si_illusion_larcis_q10361_04.htm";
			}
		}
		else if(npc.getNpcId() == CHESHA)
		{
			if(reply == 1)
			{
				return "si_illusion_chesha_q10361_02.htm";
			}
			if(reply == 2)
			{
				st.giveAdena(34000, true);
				st.addExpAndSp(35000, 5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_chesha_q10361_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == LAKCIS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_larcis_q10361_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_larcis_q10361_03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "si_illusion_larcis_q10361_06.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_larcis_q10361_02.htm";
			}
		}
		else if(npc.getNpcId() == CHESHA)
		{
			if(st.getCond() == 1)
			{
				return "si_illusion_chesha_q10361_01.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_chesha_q10361_04.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 10 && player.getLevel() <= 20;
	}
} 