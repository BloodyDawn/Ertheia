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
 * Date: 10.06.13
 * Time: 12:41
 */

public class _10445_AnImpendingThreat extends Quest
{
	// Квестовые персонажи
	private static final int MATHIAS = 31340;
	private static final int TUSKA = 33839;
	private static final int BURINU = 33840;

	// Квестовые предметы
	private static final int BADGE = 36685;

	// Квестовые награды
	private static final int SOI = 37017;
	private static final int SOULSHOT = 34609;
	private static final int SPIRITSHOT = 34616;
	private static final int ELIXIR_LIFE = 30357;
	private static final int ELIXIR_MIND = 30358;

	public _10445_AnImpendingThreat()
	{
		addStartNpc(MATHIAS);
		addTalkId(MATHIAS, TUSKA, BURINU);
		questItemIds = new int[]{BADGE};
	}

	public static void main(String[] args)
	{
		new _10445_AnImpendingThreat();
	}

	@Override
	public int getQuestId()
	{
		return 10445;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "captain_mathias_q10445_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == MATHIAS)
		{
			if(reply == 1)
			{
				return "captain_mathias_q10445_03.htm";
			}
			else if(reply == 2)
			{
				return "captain_mathias_q10445_04.htm";
			}
		}
		else if(npc.getNpcId() == TUSKA)
		{
			if(st.getCond() == 1)
			{
				if(reply == 1)
				{
					return "h_barham_q10445_02.htm";
				}
				else if(reply == 2)
				{
					st.giveItem(BADGE);
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "h_barham_q10445_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == BURINU)
		{
			if(st.getCond() == 2)
			{
				if(reply == 1)
				{
					st.addExpAndSp(30873150, 308731);
					st.giveItem(SOI);
					if(st.getPlayer().isMageClass())
					{
						st.giveItems(SPIRITSHOT, 10000);
						st.giveItems(ELIXIR_MIND, 50);
					}
					else
					{
						st.giveItems(SOULSHOT, 10000);
						st.giveItems(ELIXIR_LIFE, 50);
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "h_burinu_q10445_02.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == MATHIAS)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME); // TODO: Нет диалога?
				case CREATED:
					return st.getPlayer().getLevel() < 99 ? "captain_mathias_q10445_02.htm" : "captain_mathias_q10445_01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "captain_mathias_q10445_06.htm";
					}
			}
		}
		else if(npc.getNpcId() == TUSKA)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "h_barham_q10445_01.htm";
				}
				else if(st.getCond() == 2)
				{
					return "h_barham_q10445_04.htm";
				}
			}
		}
		else if(npc.getNpcId() == BURINU)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "h_burinu_q10445_01.htm";
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
}