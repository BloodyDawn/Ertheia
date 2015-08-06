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
 * Time: 22:21
 */

public class _10311_PeacefulDaysAreOver extends Quest
{
	// Квестовые персонажи
	private static final int Слаки = 32893;
	private static final int Селина = 33032;

	public _10311_PeacefulDaysAreOver()
	{
		addStartNpc(Селина);
		addTalkId(Селина, Слаки);
	}

	public static void main(String[] args)
	{
		new _10311_PeacefulDaysAreOver();
	}

	@Override
	public int getQuestId()
	{
		return 10311;
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
			case "33031-06.htm":
				st.startQuest();
				break;
			case "32893-05.htm":
				st.addExpAndSp(7168395, 3140085);
				st.giveAdena(489220, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestState previous = player.getQuestState(_10312_AbandonedGodsCreature.class);

		if(npc.getNpcId() == Селина)
		{
			if(previous == null || !previous.isCompleted() || player.getLevel() < 90)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "33032-03.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "33032-02.htm";
				case CREATED:
					return "33032-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33032-07.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Слаки)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "32893-01.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10312_AbandonedGodsCreature.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}
}