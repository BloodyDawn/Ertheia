package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00489_InThisQuietPlace extends Quest
{
	private static final int ADVENTURER_HELPER = 33463;
	private static final int BASTIAN = 31280;

	private static final int TRACE_OF_ES = 19501;

	private static final int[] MOBS = {21646, 21647, 21648, 21649, 21650, 21651};

	private static final int DROP_CHANCE = 60;

	public _00489_InThisQuietPlace()
	{
		setMinMaxLevel(75, 79);
		addStartNpc(ADVENTURER_HELPER);
		addTalkId(ADVENTURER_HELPER);
		addTalkId(BASTIAN);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00489_InThisQuietPlace();
	}

	@Override
	public int getQuestId()
	{
		return 489;
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
			st.giveItems(TRACE_OF_ES, 1);

			if(st.getQuestItemsCount(TRACE_OF_ES) >= 77)
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
				if(player.getLevel() < 75 || player.getLevel() > 79)
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "33463-00.htm";
				}
				else
				{
					return "33463-01.htm";
				}
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
		else if(npc.getNpcId() == BASTIAN)
		{
			if(st.isCompleted())
			{
				return "31280-03.htm";
			}
			else if(st.isStarted() && st.getCond() == 1)
			{
				return "31280-01.htm";
			}
			else if(st.isStarted() && st.getCond() == 2)
			{
				st.takeItems(TRACE_OF_ES, -1);
				st.addExpAndSp(19890000, 22602330);
				st.giveAdena(426045, true);
				st.unset("cond");
				st.setState(COMPLETED);
				st.exitQuest(QuestType.DAILY);
				return "31280-02.htm";
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