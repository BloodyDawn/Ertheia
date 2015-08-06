package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.06.13
 * Time: 9:57
 */

public class _00758_TheFallenKingsMen extends Quest
{
	// Квестовые персонажи
	private static final int INTENDANT = 33407;

	// Квестовые предметы
	private static final int TRAVIS_MARK = 36392;
	private static final int REPATRIAT_SOUL = 36393;

	// Квестовая награда
	private static final int EscortBox = 999; // TODO: ID

	// Квестовые монстры
	private static final int[] MOBS = {
		19455, 23296, 23294, 23292, 23291, 23290, 23300, 23299, 23298, 23297, 23295, 23293
	};

	public _00758_TheFallenKingsMen()
	{
		addStartNpc(INTENDANT);
		addTalkId(INTENDANT);
		addKillId(MOBS);
		questItemIds = new int[]{TRAVIS_MARK, REPATRIAT_SOUL};
	}

	public static void main(String[] args)
	{
		new _00758_TheFallenKingsMen();
	}

	private void rewardAndExit(QuestState st)
	{
		if(st.getQuestItemsCount(TRAVIS_MARK) == 50)
		{
			st.takeItems(TRAVIS_MARK, 50);
			st.giveItem(EscortBox);
		}
		if(st.hasQuestItems(REPATRIAT_SOUL))
		{
			st.getPlayer().setVitalityPoints(st.getPlayer().getVitalityDataForCurrentClassIndex().getVitalityPoints() + (int) st.getQuestItemsCount(REPATRIAT_SOUL));
			st.takeItems(REPATRIAT_SOUL, -1);
		}
		st.exitQuest(QuestType.DAILY);
		st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
	}

	@Override
	public int getQuestId()
	{
		return 758;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "aden_assult_01_3rd_q0758_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == INTENDANT)
		{
			switch(reply)
			{
				case 1:
					return "aden_assult_01_3rd_q0758_04.htm";
				case 10:
					return "aden_assult_01_3rd_q0758_10.htm";
				case 11:
					rewardAndExit(st);
					return "aden_assult_01_3rd_q0758_11.htm";
				case 13:
					return "aden_assult_01_3rd_q0758_13.htm";
				case 20:
					rewardAndExit(st);
					return "aden_assult_01_3rd_q0758_11.htm";
				case 21:
					if(st.hasQuestItems(REPATRIAT_SOUL))
					{
						st.takeItems(TRAVIS_MARK, 50);
						st.giveItem(EscortBox);
						st.exitQuest(QuestType.DAILY);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "aden_assult_01_3rd_q0758_17.htm";
					}
					else
					{
						st.takeItems(TRAVIS_MARK, 50);
						st.giveItem(EscortBox);
						st.exitQuest(QuestType.DAILY);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "aden_assult_01_3rd_q0758_15.htm";
					}
				case 31:
					if(st.hasQuestItems(REPATRIAT_SOUL))
					{
						player.setVitalityPoints(player.getVitalityDataForCurrentClassIndex().getVitalityPoints() + (int) st.getQuestItemsCount(REPATRIAT_SOUL));
						st.takeItems(REPATRIAT_SOUL, -1);
						return "aden_assult_01_3rd_q0758_18.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(st != null && st.isStarted() && ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			if(st.getCond() == 1)
			{
				st.giveItem(TRAVIS_MARK);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(TRAVIS_MARK) == 50)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(st.getCond() == 2)
			{
				st.giveItem(REPATRIAT_SOUL);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(REPATRIAT_SOUL) == 1200)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == INTENDANT)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "aden_assult_01_3rd_q0758_03.htm";
				case CREATED:
					if(player.getLevel() < 97)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "aden_assult_01_3rd_q0758_02.htm";
					}
					else
					{
						return "aden_assult_01_3rd_q0758_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						if(st.getQuestItemsCount(TRAVIS_MARK) < 50)
						{
							return "aden_assult_01_3rd_q0758_07.htm";
						}
					}
					else if(st.getCond() == 2)
					{
						return st.hasQuestItems(REPATRIAT_SOUL) ? "aden_assult_01_3rd_q0758_09.htm" : "aden_assult_01_3rd_q0758_08.htm";
					}
					else if(st.getCond() == 3)
					{
						return "aden_assult_01_3rd_q0758_09.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;

	}
}