package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00019_GoToThePastureland extends Quest
{
	// NPC
	private static final int Vladimir = 31302;
	private static final int Tunatun = 31537;

	// Items
	private static final int Veal = 15532;
	private static final int YoungWildBeastMeat = 7547;

	public _00019_GoToThePastureland()
	{
		addStartNpc(Vladimir);
		addTalkId(Vladimir);
		addTalkId(Tunatun);
		questItemIds = new int[]{Veal, YoungWildBeastMeat};
	}

	public static void main(String[] args)
	{
		new _00019_GoToThePastureland();
	}

	@Override
	public int getQuestId()
	{
		return 19;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("31302-02.htm"))
		{
			st.startQuest();
			st.giveItems(Veal, 1);
		}
		else if(event.equalsIgnoreCase("31537-02.html"))
		{
			if(st.hasQuestItems(YoungWildBeastMeat))
			{
				st.takeItems(YoungWildBeastMeat, -1);
				st.giveAdena(50000, true);
				st.addExpAndSp(136766, 12688);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.exitQuest(QuestType.ONE_TIME);
				return "31537-02.html";
			}
			else if(st.hasQuestItems(Veal))
			{
				st.takeItems(Veal, -1);
				st.giveAdena(147200, true);
				st.addExpAndSp(385040, 75250);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.exitQuest(QuestType.ONE_TIME);
				return "31537-02.html";
			}
			else
			{
				return "31537-03.html";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Vladimir)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 82)
					{
						return "31302-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "31302-03.html";
					}
				case STARTED:
					return "31302-04.html";
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			}
		}
		else if(npc.getNpcId() == Tunatun && st.getCond() == 1)
		{
			return "31537-01.html";
		}
		return null;
	}
}