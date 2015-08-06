package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.03.12
 * Time: 9:15
 */

public class _00491_ForTheSakeOfTheFather extends Quest
{
	// Квестовые персонажи
	private static final int Ширик = 33649;

	// Квестовые монстры
	private static final int[] Монстры = {23181, 23182, 23183, 23184};

	// Квестовые предметы
	private static final int ЧастицаРазлома = 34768;

	public _00491_ForTheSakeOfTheFather()
	{
		addStartNpc(Ширик);
		addTalkId(Ширик);
		addKillId(Монстры);
		questItemIds = new int[]{ЧастицаРазлома};
	}

	public static void main(String[] args)
	{
		new _00491_ForTheSakeOfTheFather();
	}

	@Override
	public int getQuestId()
	{
		return 491;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equals("33649-06.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("reward"))
		{
			st.addExpAndSp(19000000, 21328000);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.DAILY);
			return "33649-09" + Rnd.get(1, 4) + ".htm";
		}

		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			if(Rnd.getChance(50) && ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				if(st.getQuestItemsCount(ЧастицаРазлома) < 50)
				{
					st.giveItem(ЧастицаРазлома);
					if(st.getQuestItemsCount(ЧастицаРазлома) >= 50)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Ширик)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "33649-03.htm";
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() < 82 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						return "33649-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33649-02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33649-07.htm";
					}
					if(st.getCond() == 2 && st.getQuestItemsCount(ЧастицаРазлома) >= 50)
					{
						return "33649-08.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 && player.getLevel() < 82 && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}
}