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
 * Date: 18.03.13
 * Time: 12:10
 */

public class _00110_ToThePrimevalIsle extends Quest
{
	// Квестовые персонажи
	private static final int ANTON = 31338;
	private static final int MARQUEZ = 32113;

	public _00110_ToThePrimevalIsle()
	{
		addStartNpc(ANTON);
		addTalkId(ANTON, MARQUEZ);
		questItemIds = new int[]{8777};
	}

	public static void main(String[] args)
	{
		new _00110_ToThePrimevalIsle();
	}

	@Override
	public int getQuestId()
	{
		return 110;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(st != null && event.equals("quest_accept") && !st.isCompleted())
		{
			if(!st.isStarted())
			{
				st.startQuest();
				st.giveItems(8777, 1);
				st.setMemoState(1);
			}
			return "scroll_seller_anton_q0110_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == ANTON)
		{
			if(reply == 1)
			{
				return "scroll_seller_anton_q0110_03.htm";
			}
			else if(reply == 2)
			{
				return "scroll_seller_anton_q0110_04.htm";
			}
		}
		else if(npc.getNpcId() == MARQUEZ)
		{
			switch(reply)
			{
				case 3:
					if(st.getMemoState() == 1)
					{
						return "marquez_q0110_03.htm";
					}
					break;
				case 4:
					return "marquez_q0110_04.htm";
				case 5:
					if(st.isStarted() && st.getMemoState() == 1)
					{
						st.giveAdena(189208, true);
						st.addExpAndSp(887732, 983212);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "marquez_q0110_05.htm";
					}
					break;
				case 6:
					if(st.isStarted() && st.getMemoState() == 1)
					{
						st.giveAdena(189208, true);
						st.addExpAndSp(887732, 983212);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "marquez_q0110_06.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == ANTON)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() < 75 ? "scroll_seller_anton_q0110_02.htm" : "scroll_seller_anton_q0110_01.htm";
				case STARTED:
					if(st.getMemoState() == 1)
					{
						return "scroll_seller_anton_q0110_07.htm";
					}
				case COMPLETED:
					return "finishedquest.htm";
			}
		}
		else if(npc.getNpcId() == MARQUEZ)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 1)
				{
					return "marquez_q0110_01.htm";
				}
			}
		}
		return null;
	}
}