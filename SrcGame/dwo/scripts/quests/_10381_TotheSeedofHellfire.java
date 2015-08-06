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
 * Date: 30.08.12
 * Time: 22:50
 */

public class _10381_TotheSeedofHellfire extends Quest
{
	// Квестовые персонажи
	private static final int Кацерус = 32548;
	private static final int Кбальдир = 32733;
	private static final int Сизрак = 33669;

	// Квестовые предметы
	private static final int ДокументКовальдира = 34957;

	public _10381_TotheSeedofHellfire()
	{
		addStartNpc(Кацерус);
		addTalkId(Кацерус, Кбальдир, Сизрак);
		questItemIds = new int[]{ДокументКовальдира};
	}

	public static void main(String[] args)
	{
		new _10381_TotheSeedofHellfire();
	}

	@Override
	public int getQuestId()
	{
		return 10381;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "kserth_q10381_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Кацерус)
		{
			if(reply == 1)
			{
				return "kserth_q10381_02.htm";
			}
		}
		else if(npcId == Кбальдир)
		{
			if(reply == 1 && cond == 1)
			{
				return "kbarldire_q10381_02.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.setCond(2);
				st.giveItem(ДокументКовальдира);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "kbarldire_q10381_03.htm";
			}
		}
		else if(npcId == Сизрак)
		{
			if(reply == 1 && cond == 2)
			{
				return "sofa_sizraku_q10381_02.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.addExpAndSp(951127800, 435041400);
				st.giveAdena(3256740, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "sofa_sizraku_q10381_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Кацерус)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "kserth_q10381_05.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 97 ? "kserth_q10381_04.htm" : "kserth_q10381_01.htm";
				case STARTED:
					return "kserth_q10381_06.htm";
			}
		}
		else if(npcId == Кбальдир)
		{
			if(st.isStarted())
			{
				if(cond == 1)
				{
					return "kbarldire_q10381_01.htm";
				}
				else if(cond == 2)
				{
					return "kbarldire_q10381_04.htm";
				}
			}
		}
		else if(npcId == Сизрак)
		{
			if(st.isStarted())
			{
				if(cond == 2)
				{
					return "sofa_sizraku_q10381_01.htm";
				}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;
	}
}