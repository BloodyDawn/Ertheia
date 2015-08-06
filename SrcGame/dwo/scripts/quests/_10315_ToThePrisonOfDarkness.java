package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.04.12
 * Time: 23:16
 */

public class _10315_ToThePrisonOfDarkness extends Quest
{
	// Квестовые персонажи
	private static final int Слаки = 32893;
	private static final int Опера = 32946;

	public _10315_ToThePrisonOfDarkness()
	{
		addStartNpc(Слаки);
		addTalkId(Слаки, Опера);
	}

	public static void main(String[] args)
	{
		new _10315_ToThePrisonOfDarkness();
	}

	@Override
	public int getQuestId()
	{
		return 10315;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "32893-06.htm":
				st.startQuest();
				break;
			case "32946-05.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(4038093, 1708398);
				st.giveAdena(279513, true);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestState previous = player.getQuestState(_10306_FallenLeader.class);
		QuestState previous2 = player.getQuestState(_10311_PeacefulDaysAreOver.class);

		if(npc.getNpcId() == Слаки)
		{
			if(previous == null || !previous.isCompleted() || previous2 == null || !previous2.isCompleted() || player.getLevel() < 90)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32893-03.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "32893-04.htm";
				case CREATED:
					return "32893-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32893-07.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Опера)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "32946-01.htm";
				}
			}
			else
			{
				return st.isCompleted() ? "32946-03.htm" : "32946-02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10306_FallenLeader.class);
		QuestState previous2 = player.getQuestState(_10311_PeacefulDaysAreOver.class);
		return (previous != null && previous.isCompleted() || previous2 != null && previous2.isCompleted()) && player.getLevel() >= 90;

	}
}
