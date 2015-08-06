package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00487_OpenSecret extends Quest
{
	private static final int ADVENTURER_HELPER = 33463;
	private static final int PAMELA = 31600;

	private static final int DANCER_GOODS = 19499;

	private static final int[] MOBS = {21306, 21308, 21309, 21310, 21311};

	private static final int DROP_CHANCE = 60;

	public _00487_OpenSecret()
	{
		setMinMaxLevel(75, 78);
		addStartNpc(ADVENTURER_HELPER);
		addTalkId(ADVENTURER_HELPER);
		addTalkId(PAMELA);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00487_OpenSecret();
	}

	@Override
	public int getQuestId()
	{
		return 487;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == ADVENTURER_HELPER && event.equalsIgnoreCase("33463-04.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());
		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && Rnd.getChance(DROP_CHANCE))
		{
			st.giveItems(DANCER_GOODS, 1);

			if(st.getQuestItemsCount(DANCER_GOODS) >= 30)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == ADVENTURER_HELPER)
		{
			if(st.isCreated())
			{
				return "33463-01.htm";
			}
			else if(st.isStarted() && st.getCond() == 1)
			{
				return "33463-05.htm";
			}
			else if(st.isCompleted())
			{
				return getAlreadyCompletedMsg(player, QuestType.DAILY);
			}
		}
		else if(npc.getNpcId() == PAMELA)
		{
			if(st.isCompleted())
			{
				return "31600-03.htm";
			}
			else if(st.isStarted() && st.getCond() == 1)
			{
				if(st.getCond() == 1)
				{
					return "31600-01.htm";
				}
				else if(st.getCond() == 2)
				{
					st.takeItems(DANCER_GOODS, -1);
					st.addExpAndSp(26216250, 29791275);
					st.giveAdena(561555, true);
					st.unset("cond");
					st.setState(COMPLETED);
					st.exitQuest(QuestType.DAILY);
					return "31600-02.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 75 && player.getLevel() < 79;

	}
}