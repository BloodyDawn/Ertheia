package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _10364_ObligationsOfTheSeeker extends Quest
{
	//npc
	private static final int CELIN = 33451;
	private static final int WALTER = 33452;
	private static final int DEP = 33453;
	// item
	private static final int PAPER = 17578;
	private static final int[] MOBS = {22994, 22996};

	public _10364_ObligationsOfTheSeeker()
	{
		addStartNpc(CELIN);
		addTalkId(CELIN, WALTER, DEP);
		addKillId(MOBS);
		questItemIds = new int[]{PAPER};
	}

	public static void main(String[] args)
	{
		new _10364_ObligationsOfTheSeeker();
	}

	@Override
	public int getQuestId()
	{
		return 10364;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_selin_q10364_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == CELIN)
		{
			if(reply == 1)
			{
				return "si_illusion_selin_q10364_04.htm";
			}
		}
		if(npc.getNpcId() == WALTER)
		{
			if(reply == 1)
			{
				return "si_illusion_walter_q10364_02.htm";
			}
			else if(reply == 2)
			{
				return "si_illusion_walter_q10364_03.htm";
			}
			else if(reply == 3)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "si_illusion_walter_q10364_04.htm";
			}
		}
		else if(npc.getNpcId() == DEP)
		{
			if(reply == 1)
			{
				return "si_illusion_def_q10364_02.htm";
			}
			else if(reply == 2)
			{
				return "si_illusion_def_q10364_03.htm";
			}
			else if(reply == 3)
			{
				st.giveAdena(55000, true);
				st.addExpAndSp(95000, 22);
				st.giveItems(37, 1);
				st.giveItems(1060, 50);
				st.takeItems(PAPER, -1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_def_q10364_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(npc == null || st == null)
		{
			return null;
		}
		if(st.getCond() == 2)
		{
			if(Rnd.getChance(50))
			{
				st.giveItems(PAPER, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		if(st.getQuestItemsCount(PAPER) >= 5)
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == CELIN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_selin_q10364_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_selin_q10364_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "si_illusion_selin_q10364_06.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_selin_q10364_03.htm";
			}
		}
		else if(npc.getNpcId() == WALTER)
		{
			if(st.getCond() == 1)
			{
				return "si_illusion_walter_q10364_01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "si_illusion_walter_q10364_05.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_walter_q10364_07.htm";
			}
		}
		else if(npc.getNpcId() == DEP)
		{
			if(st.getCond() == 3)
			{
				return "si_illusion_def_q10364_01.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_def_q10364_05.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10363_RequestOfTheSeeker.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 14 && player.getLevel() <= 25;
	}
} 