package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.06.13
 * Time: 14:10
 */

public class _10447_TimingisEverything extends Quest
{
	// Квестовые персонажи
	private static final int BURINU = 33840;

	// Квестовые мобы
	private static final int[] MOBS = {
		23314, 23315, 23316, 23317, 23318, 23319, 23320, 23321, 23322, 23323, 23324, 23325, 23326, 23327, 23328, 23329
	};
	// Квестовые предметы
	private static final int KEY = 36665;

	public _10447_TimingisEverything()
	{
		addStartNpc(BURINU);
		addTalkId(BURINU);
		addKillId(MOBS);
		questItemIds = new int[]{KEY};
	}

	public static void main(String[] args)
	{
		new _10447_TimingisEverything();
	}

	@Override
	public int getQuestId()
	{
		return 10447;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "h_burinu_q10447_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == BURINU)
		{
			if(reply == 1)
			{
				return "h_burinu_q10447_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());
		if(Rnd.getChance(0.5) && st != null && st.isStarted())
		{
			if(st.getCond() == 1)
			{
				st.giveItem(KEY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == BURINU)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					QuestState prevSt = st.getPlayer().getQuestState(_10445_AnImpendingThreat.class);
					if(st.getPlayer().getLevel() >= 99 && prevSt != null && prevSt.isCompleted() && !st.getPlayer().isNoble())
					{
						return "h_burinu_q10447_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "h_burinu_q10447_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "h_burinu_q10447_05.htm";
					}
					else if(st.getCond() == 2)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						st.addExpAndSp(2147483647, 22228668);
						return "h_burinu_q10447_06.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(_10445_AnImpendingThreat.class);
		return player.getLevel() >= 99 && st != null && st.isCompleted();
	}
}