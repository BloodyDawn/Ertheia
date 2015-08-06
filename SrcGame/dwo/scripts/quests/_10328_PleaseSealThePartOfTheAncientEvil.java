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
 * Date: 20.11.11
 * Time: 22:54
 */

public class _10328_PleaseSealThePartOfTheAncientEvil extends Quest
{
	private static final int KEKIY = 30565;
	private static final int PANTEON = 32972;

	public _10328_PleaseSealThePartOfTheAncientEvil()
	{
		addStartNpc(PANTEON);
		addTalkId(KEKIY, PANTEON);
	}

	public static void main(String[] args)
	{
		new _10328_PleaseSealThePartOfTheAncientEvil();
	}

	@Override
	public int getQuestId()
	{
		return 10328;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_pantheon_q10328_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == PANTEON)
		{
			if(reply == 1)
			{
				return "si_illusion_pantheon_q10328_04.htm";
			}
			else if(reply == 2)
			{
				return "si_illusion_pantheon_q10328_05.htm";
			}
		}
		else if(npc.getNpcId() == KEKIY)
		{
			if(reply == 1)
			{
				return "kakai_the_lord_of_flame_q10328_03.htm";
			}
			if(reply == 2)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(40, 5);
				st.giveAdena(5000, true);
				st.exitQuest(QuestType.ONE_TIME);
				return "kakai_the_lord_of_flame_q10328_05.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == PANTEON)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_illusion_pantheon_q10328_03.htm";
				case CREATED:
					if(canBeStarted(st.getPlayer()))
					{
						return "si_illusion_pantheon_q10328_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_pantheon_q10328_02.htm";
					}
				case STARTED:
					return "si_illusion_pantheon_q10328_07.htm";
			}
		}
		else if(npc.getNpcId() == KEKIY)
		{
			if(st.isCompleted())
			{
				return "kakai_the_lord_of_flame_q10328_02.htm";
			}
			else if(st.getCond() == 1)
			{
				return "kakai_the_lord_of_flame_q10328_01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10327_IntruderWhoWantsTheBookOfGiants.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;

	}
}
