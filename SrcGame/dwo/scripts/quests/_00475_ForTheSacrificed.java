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
 * @author ANZO
 * Date: 24.04.12
 * Time: 13:11
 */

public class _00475_ForTheSacrificed extends Quest
{
	// Квестовые персонажи
	private static final int Гид = 33463;
	private static final int Росс = 30858;

	// Квестовые предметы
	private static final int ПрахМонстра = 19495;

	// Квестовые монстры
	private static final int[] Монстры = {
		20676, 20677, 21108, 21109, 21110, 21111, 21112, 20773, 21113, 21114, 21115, 21116
	};

	public _00475_ForTheSacrificed()
	{
		setMinMaxLevel(65, 69);
		addStartNpc(Гид);
		addTalkId(Гид, Росс);
		addKillId(Монстры);
		questItemIds = new int[]{ПрахМонстра};
	}

	public static void main(String[] args)
	{
		new _00475_ForTheSacrificed();
	}

	@Override
	public int getQuestId()
	{
		return 475;
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
			case "33463-04.htm":
				st.startQuest();
				break;
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

		if(st.getCond() == 1 && Rnd.getChance(50))
		{
			if(ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				st.giveItem(ПрахМонстра);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(ПрахМонстра) >= 30)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
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

		if(npc.getNpcId() == Гид)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					return st.getCond() == 1 ? "33463-05.htm" : "33463-06.htm";
			}
		}
		else if(npc.getNpcId() == Росс)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "30858-02.htm";
				}
				else if(st.getCond() == 2)
				{
					st.addExpAndSp(3904500, 2813550);
					st.giveAdena(118500, true);
					st.exitQuest(QuestType.DAILY);
					return "30858-01.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "30858-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 65 && player.getLevel() <= 69;

	}
}