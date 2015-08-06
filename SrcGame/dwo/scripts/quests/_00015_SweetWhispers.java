package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00015_SweetWhispers extends Quest
{
	// Квестовые пероснажи
	private static final int VLADIMIR = 31302;
	private static final int PRESBYTER = 31517;
	private static final int DARK_NECROMANCER = 31518;

	public _00015_SweetWhispers()
	{
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR, PRESBYTER, DARK_NECROMANCER);
	}

	public static void main(String[] args)
	{
		new _00015_SweetWhispers();
	}

	@Override
	public int getQuestId()
	{
		return 15;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() >= 60 && !st.isCompleted())
		{
			st.startQuest();
			return "trader_vladimir_q0015_0104.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(reply == 1 && npcId == DARK_NECROMANCER && cond == 1)
		{
			st.setCond(2);
			return "dark_necromancer_q0015_0201.htm";
		}
		if(reply == 3 && npcId == PRESBYTER && cond == 2)
		{
			st.addExpAndSp(714215, 650980);
			st.unset("cond");
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
			return "dark_presbyter_q0015_0301.htm";
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		int cond = st.getCond();
		if(npc.getNpcId() == VLADIMIR)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
				{
					return "trader_vladimir_q0015_0101.htm";
				}
				else
				{

					st.exitQuest(QuestType.REPEATABLE);
					return "trader_vladimir_q0015_0103.htm";
				}
			}
			else if(cond >= 1)
			{
				return "trader_vladimir_q0015_0105.htm";
			}
		}
		else if(npc.getNpcId() == DARK_NECROMANCER)
		{
			if(cond == 1)
			{
				return "dark_necromancer_q0015_0101.htm";
			}
			else if(cond == 2)
			{
				return "dark_necromancer_q0015_0202.htm";
			}
		}
		else if(npc.getNpcId() == PRESBYTER)
		{
			if(cond == 2)
			{
				return "dark_presbyter_q0015_0201.htm";
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}