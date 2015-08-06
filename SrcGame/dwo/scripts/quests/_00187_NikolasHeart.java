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
 * Date: 14.08.12
 * Time: 20:10
 */

public class _00187_NikolasHeart extends Quest
{
	// Квестовые персонажи
	private static final int Kusto = 30512;
	private static final int Nikola = 30621;
	private static final int Lorain = 30673;

	// Квестовые предметы
	private static final int Certificate = 10362;
	private static final int Metal = 10368;

	public _00187_NikolasHeart()
	{
		addStartNpc(Lorain);
		addTalkId(Kusto, Nikola, Lorain);
		questItemIds = new int[]{Certificate, Metal};
	}

	public static void main(String[] args)
	{
		new _00187_NikolasHeart();
	}

	@Override
	public int getQuestId()
	{
		return 187;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			st.takeItems(Certificate, -1);
			st.giveItems(Metal, 1);
			return "researcher_lorain_q0187_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Nikola)
		{
			if(reply == 1)
			{
				return "maestro_nikola_q0187_02.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "maestro_nikola_q0187_03.htm";
			}
		}
		else if(npcId == Kusto)
		{
			if(reply == 1)
			{
				return "head_blacksmith_kusto_q0187_02.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.giveAdena(110336, true);
				st.addExpAndSp(549120, 377296);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "head_blacksmith_kusto_q0187_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Lorain)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState pqs = st.getPlayer().getQuestState(_00185_NikolasCooperationConsideration.class);
					if(st.getPlayer().getLevel() < 41)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "researcher_lorain_q0187_02.htm";
					}
					if(pqs != null && pqs.isCompleted())
					{
						return "researcher_lorain_q0187_01.htm";
					}
					return getNeedCompletedQuest(186);
				case STARTED:
					return "researcher_lorain_q0187_04.htm";
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
		}
		else if(npcId == Nikola)
		{
			if(cond == 1)
			{
				return "maestro_nikola_q0187_01.htm";
			}
			else if(cond == 2)
			{
				return "maestro_nikola_q0187_04.htm";
			}
		}
		else if(npcId == Kusto)
		{
			if(cond == 2)
			{
				return "head_blacksmith_kusto_q0187_01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pqs = player.getQuestState(_00185_NikolasCooperationConsideration.class);
		return player.getLevel() >= 41 && pqs != null && pqs.isCompleted();
	}
}