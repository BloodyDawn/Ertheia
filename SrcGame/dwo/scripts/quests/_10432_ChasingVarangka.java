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
 * Date: 18.06.13
 * Time: 21:13
 */

public class _10432_ChasingVarangka extends Quest
{
	// Квестовые персонажи
	private static final int CHEIREN = 32655;
	private static final int JOKEL = 33868;

	// Квестовые монстры
	private static final int FARANGA_RAID = 25509;

	private static final int[] CLASS_LIMITS = {88, 90, 91, 93, 99, 100, 101, 106, 107, 108, 114, 131, 132, 133, 136};

	// Квестовая награда
	private static final int IRON_GATE_COIN = 37045;

	public _10432_ChasingVarangka()
	{
		addStartNpc(CHEIREN);
		addTalkId(CHEIREN, JOKEL);
		addKillId(FARANGA_RAID);
	}

	public static void main(String[] args)
	{
		new _10432_ChasingVarangka();
	}

	@Override
	public int getQuestId()
	{
		return 10432;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "cheiren_q10432_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == CHEIREN)
		{
			if(reply == 1)
			{
				return "cheiren_q10432_03.htm";
			}
			else if(reply == 2)
			{
				return "cheiren_q10432_04.htm";
			}
		}
		else if(npc.getNpcId() == JOKEL)
		{
			if(reply == 10 && st.getCond() == 1)
			{
				st.addExpAndSp(14120400, 141204);
				st.giveItems(IRON_GATE_COIN, 30);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "yokel_q10432_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == FARANGA_RAID)
		{
			executeForEachPlayer(player, npc, isPet, true, false);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == CHEIREN)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					QuestState prevSt = player.getQuestState(_10431_TheSealofPunishmentDenofEvil.class);
					if(prevSt != null && prevSt.isCompleted() && player.getLevel() >= 81 && player.getLevel() <= 84 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId()))
					{
						return "cheiren_q10432_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "cheiren_q10432_02.htm";
					}
				case STARTED:
					return "cheiren_q10432_06.htm";
			}
		}
		else if(npc.getNpcId() == JOKEL)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "yokel_q10432_01.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState prevSt = player.getQuestState(_10431_TheSealofPunishmentDenofEvil.class);
		return prevSt != null && prevSt.isCompleted() && player.getLevel() >= 81 && player.getLevel() <= 84 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId());

	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1)
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}
}