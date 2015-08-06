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
 * Time: 21:24
 */

public class _00483_IntendedTactic extends Quest
{
	// Квестовые персонажи
	private static final int Энде = 33357;

	// Квестовые предметы
	private static final int КровьВерности = 17736;
	private static final int КровьИстины = 17736;
	private static final int СимволДерзости = 17624;

	// Квестовые монстры
	private static final int[] МонстрыВерности = {23069, 23070, 23073, 23071, 23072, 23074, 23075};
	private static final int[] МонстрыИстины = {25811, 25812, 25815, 25809};

	public _00483_IntendedTactic()
	{
		addStartNpc(Энде);
		addTalkId(Энде);
		addKillId(МонстрыВерности);
		addKillId(МонстрыИстины);
		questItemIds = new int[]{КровьВерности, КровьИстины};
	}

	public static void main(String[] args)
	{
		new _00483_IntendedTactic();
	}

	@Override
	public int getQuestId()
	{
		return 483;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33357-08.htm"))
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
			return super.onKill(npc, player, isPet);
		}

		if(st.getCond() == 1)
		{
			if(ArrayUtils.contains(МонстрыВерности, npc.getNpcId()) && Rnd.getChance(25))
			{
				st.giveItem(КровьВерности);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(КровьВерности) >= 10)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(ArrayUtils.contains(МонстрыИстины, npc.getNpcId()))
			{
				if(!st.hasQuestItems(КровьИстины))
				{
					st.giveItem(КровьИстины);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		else if(st.getCond() == 2)
		{
			if(ArrayUtils.contains(МонстрыИстины, npc.getNpcId()))
			{
				if(!st.hasQuestItems(КровьИстины))
				{
					st.giveItem(КровьИстины);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
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

		if(npc.getNpcId() == Энде)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 48)
					{
						return "33357-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33357-02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33357-09.htm";
					}
					if(st.getCond() == 2)
					{
						if(st.getQuestItemsCount(КровьВерности) >= 10 && st.getQuestItemsCount(КровьИстины) >= 1)
						{
							st.addExpAndSp(1500000, 1250000);
							st.giveItem(СимволДерзости);
							st.exitQuest(QuestType.DAILY);
							return "33357-12.htm";
						}
						else if(st.getQuestItemsCount(КровьВерности) >= 10)
						{
							st.addExpAndSp(1500000, 1250000);
							st.exitQuest(QuestType.DAILY);
							return "33357-11.htm";
						}
					}
					break;
				case COMPLETED:
					return "33357-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 48;

	}
}