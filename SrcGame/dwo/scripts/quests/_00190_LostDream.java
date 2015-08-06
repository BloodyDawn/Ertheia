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
 * Date: 13.08.12
 * Time: 13:57
 */

public class _00190_LostDream extends Quest
{
	// Квестовые персонажи
	private static final int Kusto = 30512;
	private static final int Nikola = 30621;
	private static final int Lorain = 30673;
	private static final int Juris = 30113;

	public _00190_LostDream()
	{
		addStartNpc(Kusto);
		addTalkId(Kusto, Lorain, Nikola, Juris);
	}

	public static void main(String[] args)
	{
		new _00190_LostDream();
	}

	@Override
	public int getQuestId()
	{
		return 190;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "head_blacksmith_kusto_q0190_03.htm";
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
			if(reply == 1 && cond == 2)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "head_blacksmith_kusto_q0190_06.htm";
			}
		}
		else if(npcId == Juris)
		{
			if(reply == 1)
			{
				return "juria_q0190_02.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "juria_q0190_03.htm";
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
					QuestState pqs = st.getPlayer().getQuestState(_00187_NikolasHeart.class);
					if(st.getPlayer().getLevel() >= 42)
					{
						return pqs != null && pqs.isCompleted() ? "head_blacksmith_kusto_q0190_01.htm" : getNeedCompletedQuest(187);
					}
					else
					{
						return "head_blacksmith_kusto_q0190_02.htm";
					}
				case STARTED:
					switch(cond)
					{
						case 1:
							return "head_blacksmith_kusto_q0190_04.htm";
						case 2:
							return "head_blacksmith_kusto_q0190_05.htm";
						case 3:
							return "head_blacksmith_kusto_q0190_07.htm";
						case 5:
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.giveAdena(127224, true);
							st.addExpAndSp(634158, 443538);
							st.exitQuest(QuestType.ONE_TIME);
							return "head_blacksmith_kusto_q0190_08.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
		}
		else if(npcId == Juris)
		{
			if(st.isStarted())
			{
				return cond == 1 ? "juria_q0190_01.htm" : "juria_q0190_04.htm";
			}
		}
		else if(npcId == Lorain)
		{
			if(st.isStarted())
			{
				if(cond == 3)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "researcher_lorain_q0190_01.htm";
				}
				else
				{
					return "researcher_lorain_q0190_02.htm";
				}
			}
		}
		else if(npcId == Nikola)
		{
			if(st.isStarted())
			{
				if(cond == 4)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "maestro_nikola_q0190_01.htm";
				}
				else
				{
					return "maestro_nikola_q0190_02.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pqs = player.getQuestState(_00187_NikolasHeart.class);
		return player.getLevel() >= 42 && pqs != null && pqs.isCompleted();
	}
}