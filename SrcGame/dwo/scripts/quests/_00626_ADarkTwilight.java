package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 4:49
 */

public class _00626_ADarkTwilight extends Quest
{
	// Квестовые персонажи
	private static final int HIERARCH = 31517;

	// Квестовые предметы
	private static final int BLOOD_OF_SAINT = 7169;

	public _00626_ADarkTwilight()
	{
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH);
		questItemIds = new int[]{BLOOD_OF_SAINT};
		for(int npcId = 21520; npcId <= 21542; npcId++)
		{
			addKillId(npcId);
		}
	}

	public static void main(String[] args)
	{
		new _00626_ADarkTwilight();
	}

	@Override
	public int getQuestId()
	{
		return 626;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "dark_presbyter_q0626_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == HIERARCH)
		{
			if(reply == 3 && cond == 2)
			{
				return "dark_presbyter_q0626_0201.htm";
			}
			else if(reply == 11 && cond == 2)
			{
				st.addExpAndSp(162773, 12500);
				st.exitQuest(QuestType.REPEATABLE);
				return "dark_presbyter_q0626_0202.htm";
			}
			else if(reply == 12 && cond == 2)
			{
				st.giveAdena(100000, true);
				st.exitQuest(QuestType.REPEATABLE);
				return "dark_presbyter_q0626_0202.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(st.getCond() == 1 && Rnd.getChance(70))
		{
			st.giveItems(BLOOD_OF_SAINT, 1);
			if(st.getQuestItemsCount(BLOOD_OF_SAINT) == 300)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == HIERARCH)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() < 62)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "dark_presbyter_q0626_0103.htm";
					}
					else
					{
						return "dark_presbyter_q0626_0101.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "dark_presbyter_q0626_0106.htm";
					}
					else if(cond == 2)
					{
						if(st.getQuestItemsCount(BLOOD_OF_SAINT) < 300)
						{
							st.setCond(1);
							return "dark_presbyter_q0626_0203.htm";
						}
						else
						{
							return "dark_presbyter_q0626_0105.htm";
						}
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 62;
	}
}