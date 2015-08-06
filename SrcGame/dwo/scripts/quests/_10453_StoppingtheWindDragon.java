package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.06.13
 * Time: 20:05
 */

public class _10453_StoppingtheWindDragon extends Quest
{
	// Квестовые персонажи
	private static final int JANINE = 33872;

	// Квестовые награды
	private static final int DIADEM = 37497;

	// Квестовые монстры
	private static final int LINDVIOR = 29240;

	public _10453_StoppingtheWindDragon()
	{
		addStartNpc(JANINE);
		addTalkId(JANINE);
		addKillId(JANINE);
	}

	public static void main(String[] args)
	{
		new _10453_StoppingtheWindDragon();
	}

	@Override
	public int getQuestId()
	{
		return 10453;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "janine_q10453_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == JANINE)
		{
			switch(reply)
			{
				case 1:
					return "janine_q10453_03.htm";
				case 2:
					return "janine_q10453_04.htm";
				case 3:
					return "janine_q10453_05.htm";
				case 10:
					if(st.getCond() == 2)
					{
						st.giveItem(DIADEM);
						st.addExpAndSp(2147483500, 37047780);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "janine_q10453_08.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if(npc.getNpcId() == LINDVIOR)
		{
			executeForEachPlayer(killer, npc, isSummon, true, true);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == LINDVIOR)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					return st.getPlayer().getLevel() >= 99 ? "janine_q10453_01.htm" : "janine_q10453_02.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "janine_q10453_06.htm";
					}
					else if(st.getCond() == 2)
					{
						return "janine_q10453_07.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 99;
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1 && Util.checkIfInRange(1500, npc, player, false))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}
}