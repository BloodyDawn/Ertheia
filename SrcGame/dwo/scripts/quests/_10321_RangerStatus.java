package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.08.11
 * Time: 13:43
 */

public class _10321_RangerStatus extends Quest
{
	// Квестовые персонажи
	private static int THEODORE = 32975;
	private static int SHANNON = 32974;

	public _10321_RangerStatus()
	{
		addStartNpc(THEODORE);
		addTalkId(THEODORE, SHANNON);
	}

	public static void main(String[] args)
	{
		new _10321_RangerStatus();
	}

	@Override
	public int getQuestId()
	{
		return 10321;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_theodore_q10321_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == THEODORE)
		{
			if(reply == 1)
			{
				return "si_illusion_theodore_q10321_04.htm";
			}
		}
		else if(npc.getNpcId() == SHANNON)
		{
			if(reply == 2)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(40, 5);
				st.giveAdena(5000, true);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_shannon_q10321_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == THEODORE)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_illusion_theodore_q10321_03.htm";
				case CREATED:
					QuestState prevst = player.getQuestState(_10320_ToTheCentralSquare.class);
					if(player.getLevel() >= 20)
					{
						return "si_illusion_theodore_q10321_02.htm";
					}
					else if(prevst == null || !prevst.isCompleted())
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_theodore_q10321_03.htm"; // TODO: Нет ретейл диалога о_О
					}
					else
					{
						return "si_illusion_theodore_q10321_01.htm";
					}
				case STARTED:
					return "si_illusion_theodore_q10321_06.htm";
			}
		}
		else if(npc.getNpcId() == SHANNON)
		{
			if(player.getLevel() >= 20)
			{
				return "si_illusion_shannon_q10321_02.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_shannon_q10321_03.htm";
			}
			else if(st.isStarted())
			{
				return "si_illusion_shannon_q10321_01.htm";
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10320_ToTheCentralSquare.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;

	}
}