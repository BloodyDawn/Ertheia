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
 * Date: 22.04.12
 * Time: 1:59
 */

public class _00467_TheOppressorAndTheOppressed extends Quest
{
	// Квестовые персонажи
	private static final int ГидПутешественников = 33463;
	private static final int Дезмонд = 30855;

	// Квестовые предметы
	private static final int ЧистоеЯдро = 19488;

	// Квестовые монстры
	private static final int[] Монстры = {20650, 20648, 20647, 20649};

	public _00467_TheOppressorAndTheOppressed()
	{
		setMinMaxLevel(60, 64);
		addStartNpc(ГидПутешественников);
		addTalkId(ГидПутешественников, Дезмонд);
		addKillId(Монстры);
		questItemIds = new int[]{ЧистоеЯдро};
	}

	public static void main(String[] args)
	{
		new _00467_TheOppressorAndTheOppressed();
	}

	@Override
	public int getQuestId()
	{
		return 467;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
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
			if(ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				if(Rnd.getChance(25))
				{
					if(st.getQuestItemsCount(ЧистоеЯдро) < 30)
					{
						st.giveItem(ЧистоеЯдро);
						if(st.getQuestItemsCount(ЧистоеЯдро) >= 30)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == ГидПутешественников)
		{
			if(st.isNowAvailable() && st.isCompleted())
			{
				st.setState(CREATED);
			}

			switch(st.getState())
			{
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "33463-05.htm";
						case 2:
							return "33463-06.htm";
					}
					break;
				case COMPLETED:
					return "33463-07.htm";
			}
		}
		else if(npc.getNpcId() == Дезмонд)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					st.addExpAndSp(1879400, 1782000);
					st.giveAdena(194000, true);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.DAILY);
					return "30855-01.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 60 && player.getLevel() <= 64;

	}
}
