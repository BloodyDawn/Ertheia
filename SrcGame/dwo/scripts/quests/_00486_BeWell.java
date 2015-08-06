package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.05.12
 * Time: 23:36
 */

public class _00486_BeWell extends Quest
{
	// Квестовые персонажи
	private static final int Аберкромби = 31555;
	private static final int Гид = 33463;

	// Квестовые монстры
	private static final int[] Монстры = {
		21508, 21509, 21510, 21511, 21512, 21513, 21514, 21515, 21516, 21517, 21518, 21519
	};

	// Квестовые предметы
	private static final int ПанцирьСтакато = 19498;

	public _00486_BeWell()
	{
		setMinMaxLevel(70, 74);
		addStartNpc(Гид);
		addTalkId(Гид, Аберкромби);
		addKillId(Монстры);
		questItemIds = new int[]{ПанцирьСтакато};
	}

	public static void main(String[] args)
	{
		new _00486_BeWell();
	}

	@Override
	public int getQuestId()
	{
		return 486;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33463-04.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			if(ArrayUtils.contains(Монстры, npc.getNpcId()) && Rnd.getChance(90))
			{
				st.giveItem(ПанцирьСтакато);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(ПанцирьСтакато) >= 80)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Гид)
		{
			switch(st.getState())
			{
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33463-05.htm";
					}
					if(st.getCond() == 2)
					{
						return "33463-06.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
			}
		}
		else if(npc.getNpcId() == Аберкромби)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					st.addExpAndSp(9009000, 8997060);
					st.giveAdena(353160, true);
					st.exitQuest(QuestType.DAILY);
					return "31555-01.htm";
				}
				else
				{
					return "31555-02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31555-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 70 && player.getLevel() <= 74;

	}
}