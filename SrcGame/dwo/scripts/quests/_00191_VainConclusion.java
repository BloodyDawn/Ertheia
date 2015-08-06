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
 * Time: 17:35
 */

public class _00191_VainConclusion extends Quest
{
	// Квестовые НПЦ
	private static final int Kusto = 30512;
	private static final int Dorothy = 30970;
	private static final int Lorain = 30673;
	private static final int Shegfield = 30068;

	// Квестовые предметы
	private static final short Metallograph = 10371;

	public _00191_VainConclusion()
	{
		addStartNpc(Dorothy);
		addTalkId(Kusto, Lorain, Dorothy, Shegfield);
		questItemIds = new int[]{Metallograph};
	}

	public static void main(String[] args)
	{
		new _00191_VainConclusion();
	}

	@Override
	public int getQuestId()
	{
		return 191;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.giveItems(Metallograph, 1);
			return "dorothy_the_locksmith_q0191_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Dorothy)
		{
			if(reply == 1)
			{
				return "dorothy_the_locksmith_q0191_03.htm";
			}
		}
		else if(npcId == Lorain)
		{
			if(reply == 1 && cond == 1)
			{
				st.takeItems(Metallograph, -1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "researcher_lorain_q0191_02.htm";
			}
		}
		else if(npcId == Shegfield)
		{
			if(reply == 1 && cond == 2)
			{
				return "shegfield_q0191_02.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "shegfield_q0191_03.htm";
			}
		}
		else if(npcId == Kusto)
		{
			if(reply == 1 && cond == 4)
			{
				st.giveAdena(134292, true);
				st.addExpAndSp(669388, 468178);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "head_blacksmith_kusto_q0191_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Dorothy)
		{
			switch(st.getState())
			{
				case CREATED:

					if(st.getPlayer().getLevel() >= 42)
					{
						QuestState pqs = st.getPlayer().getQuestState(_00188_SealRemoval.class);
						return pqs != null && pqs.isCompleted() ? "dorothy_the_locksmith_q0191_01.htm" : getNeedCompletedQuest(188);
					}
					else
					{
						return "dorothy_the_locksmith_q0191_02.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "dorothy_the_locksmith_q0191_05.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
		}
		else if(npcId == Lorain)
		{
			if(st.isStarted())
			{
				switch(cond)
				{
					case 1:
						return "researcher_lorain_q0191_01.htm";
					case 2:
						return "researcher_lorain_q0191_03.htm";
					case 3:
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "researcher_lorain_q0191_04.htm";
					case 4:
						return "researcher_lorain_q0191_05.htm";
				}
			}
		}
		else if(npcId == Shegfield)
		{
			if(st.isStarted())
			{
				if(cond == 2)
				{
					return "shegfield_q0191_01.htm";
				}
				else if(cond == 3)
				{
					return "shegfield_q0191_04.htm";
				}
			}
		}
		else if(npcId == Kusto)
		{
			if(st.isStarted())
			{
				if(cond == 4)
				{
					return "head_blacksmith_kusto_q0191_01.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pqs = player.getQuestState(_00188_SealRemoval.class);
		return player.getLevel() >= 42 && pqs != null && pqs.isCompleted();
	}
}