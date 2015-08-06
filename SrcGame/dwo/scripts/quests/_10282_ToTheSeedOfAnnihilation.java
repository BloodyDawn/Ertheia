package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10282_ToTheSeedOfAnnihilation extends Quest
{
	// Квестовые персонажи
	private static final int KBALDIR = 32733;
	private static final int KLEMIS = 32734;

	// Квестовые предметы
	private static final int SOA_ORDERS = 15512;

	public _10282_ToTheSeedOfAnnihilation()
	{
		addStartNpc(KBALDIR);
		addTalkId(KBALDIR, KLEMIS);
		questItemIds = new int[]{SOA_ORDERS};
	}

	public static void main(String[] args)
	{
		new _10282_ToTheSeedOfAnnihilation();
	}

	@Override
	public int getQuestId()
	{
		return 10282;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(event.equals("32733-07.htm"))
		{
			st.startQuest();
			st.giveItems(SOA_ORDERS, 1);
		}
		else if(event.equals("32734-02.htm"))
		{
			if(st.hasQuestItems(SOA_ORDERS))
			{
				st.addExpAndSp(1148480, 99110);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
			}
			else
			{
				return "32734-04.htm";
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == KBALDIR)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 85 ? "32733-01.htm" : "32733-00.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						if(!st.hasQuestItems(SOA_ORDERS))
						{
							st.giveItem(SOA_ORDERS);
						}
						return "32733-08.htm";
					}
					break;
				case COMPLETED:
					return "32733-09.htm";
			}
		}
		else if(npc.getNpcId() == KLEMIS)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "32734-03.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32734-01.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85;

	}
}
