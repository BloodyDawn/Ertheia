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
 * Date: 13.06.13
 * Time: 23:55
 */

public class _10450_ADarkAmbition extends Quest
{
	// Квестовые персонажи
	private static final int MATIAS = 31340;
	private static final int BARHAM = 33839;

	// Квестовые награды
	private static final int SOI = 37019;
	private static final int SOULSHOT = 34609;
	private static final int SPIRITSHOT = 34616;
	private static final int ELIXIR_LIFE = 30357;
	private static final int ELIXIR_MIND = 30358;

	public _10450_ADarkAmbition()
	{
		addStartNpc(MATIAS);
		addTalkId(MATIAS, BARHAM);
	}

	public static void main(String[] args)
	{
		new _10450_ADarkAmbition();
	}

	@Override
	public int getQuestId()
	{
		return 10450;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "captain_mathias_q10450_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == MATIAS)
		{
			switch(reply)
			{
				case 1:
					return "captain_mathias_q10450_03.htm";
				case 2:
					return "captain_mathias_q10450_04.htm";
				case 3:
					return "captain_mathias_q10450_05.htm";
			}
		}
		else if(npc.getNpcId() == BARHAM)
		{
			if(st.getCond() == 1)
			{
				if(reply == 1)
				{
					return "h_barham_q10450_02.htm";
				}
				else if(reply == 2)
				{
					st.addExpAndSp(15436575, 154365);
					if(player.isMageClass())
					{
						st.giveItems(ELIXIR_MIND, 50);
						st.giveItems(SPIRITSHOT, 10000);
					}
					else
					{
						st.giveItems(ELIXIR_LIFE, 50);
						st.giveItems(SOULSHOT, 10000);
					}
					st.giveItem(SOI);
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "h_barham_q10450_03.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == MATIAS)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					if(st.getPlayer().getLevel() >= 99)
					{
						return "captain_mathias_q10450_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.ONE_TIME);
						return "captain_mathias_q10450_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "captain_mathias_q10450_07.htm";
					}
			}
		}
		else if(npc.getNpcId() == BARHAM)
		{
			if(st.isStarted())
			{
				return "h_barham_q10450_01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 99;
	}
}