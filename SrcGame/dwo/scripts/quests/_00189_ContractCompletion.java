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
 * Time: 14:33
 */

public class _00189_ContractCompletion extends Quest
{
	// Квестовые персонажи
	private static final int Luka = 31437;
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Shegfield = 30068;

	// Квестовые предметы
	private static final short Metallograph = 10370;

	public _00189_ContractCompletion()
	{
		addStartNpc(Luka);
		addTalkId(Luka, Kusto, Lorain, Shegfield);
		questItemIds = new int[]{Metallograph};
	}

	public static void main(String[] args)
	{
		new _00189_ContractCompletion();
	}

	@Override
	public int getQuestId()
	{
		return 189;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			st.giveItems(Metallograph, 1);
			return "blueprint_seller_luka_q0189_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Lorain)
		{
			if(reply == 1 && cond == 1)
			{
				st.takeItems(Metallograph, -1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "researcher_lorain_q0189_02.htm";
			}
		}
		else if(npcId == Shegfield)
		{
			if(reply == 1)
			{
				return "shegfield_q0189_02.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "shegfield_q0189_03.htm";
			}
		}
		else if(npcId == Kusto)
		{
			if(reply == 1 && cond == 3)
			{
				st.giveAdena(141360, true);
				if(st.getPlayer().getLevel() < 48)
				{
					st.addExpAndSp(704620, 492820);
				}
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "head_blacksmith_kusto_q0189_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Luka)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState pqs = st.getPlayer().getQuestState(_00186_ContractExecution.class);
					if(st.getPlayer().getLevel() < 42)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "blueprint_seller_luka_q0189_02.htm";
					}
					if(pqs != null && pqs.isCompleted())
					{
						return "blueprint_seller_luka_q0189_01.htm";
					}
					return getNeedCompletedQuest(186);
				case STARTED:
					return "blueprint_seller_luka_q0189_04.htm";
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
						return "researcher_lorain_q0189_01.htm";
					case 2:
						return "researcher_lorain_q0189_03.htm";
					case 3:
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "researcher_lorain_q0189_04.htm";
					case 4:
						return "researcher_lorain_q0189_05.htm";
				}
			}
		}
		else if(npcId == Shegfield)
		{
			if(st.isStarted())
			{
				switch(cond)
				{
					case 2:
						return "shegfield_q0189_01.htm";
					case 3:
						return "shegfield_q0189_04.htm";
				}
			}
		}
		else if(npcId == Kusto)
		{
			if(st.isStarted())
			{
				if(cond == 4)
				{
					return "head_blacksmith_kusto_q0189_01.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pqs = player.getQuestState(_00186_ContractExecution.class);
		return player.getLevel() >= 42 && pqs != null && pqs.isCompleted();
	}
}