package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 20:55
 */

public class _00183_RelicExploration extends Quest
{
	// Квестовые персонажи
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;

	public _00183_RelicExploration()
	{
		addStartNpc(Kusto);
		addTalkId(Kusto, Nikola, Lorain);
	}

	public static void main(String[] args)
	{
		new _00183_RelicExploration();
	}

	@Override
	public int getQuestId()
	{
		return 183;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "head_blacksmith_kusto_q0183_01.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Kusto)
		{
			if(reply == 1)
			{
				return "head_blacksmith_kusto_q0183_04.htm";
			}
		}
		else if(npcId == Lorain)
		{
			if(reply == 1)
			{
				return "researcher_lorain_q0183_02.htm";
			}
			else if(reply == 2)
			{
				return "researcher_lorain_q0183_03.htm";
			}
			else if(reply == 3 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "researcher_lorain_q0183_04.htm";
			}
		}
		else if(npcId == Nikola)
		{
			if(reply == 1)
			{
				st.giveAdena(26866, true);
				st.addExpAndSp(133636, 90622);
				st.exitQuest(QuestType.ONE_TIME);
				return "maestro_nikola_q0183_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Kusto)
		{
			switch(st.getState())
			{
				case CREATED:
					return st.getPlayer().getLevel() >= 40 ? "head_blacksmith_kusto_q0183_02.htm" : "head_blacksmith_kusto_q0183_03.htm";
				case STARTED:
					if(cond == 1)
					{
						return "head_blacksmith_kusto_q0183_05.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
		}
		else if(npcId == Lorain)
		{
			if(cond == 1)
			{
				return "researcher_lorain_q0183_01.htm";
			}
			else if(cond == 2)
			{
				return "researcher_lorain_q0183_05.htm";
			}
		}
		else if(npcId == Nikola)
		{
			if(cond == 2)
			{
				return "maestro_nikola_q0183_01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() < 40;
	}
}