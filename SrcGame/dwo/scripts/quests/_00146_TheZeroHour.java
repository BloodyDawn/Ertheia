package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00146_TheZeroHour extends Quest
{
	// Npc
	private static final int Kahman = 31554;
	private static final int QueenShyeed = 25671;

	// Item
	private static final int Fang = 14859;

	public _00146_TheZeroHour()
	{
		addStartNpc(Kahman);
		addTalkId(Kahman);
		addKillId(QueenShyeed);

		questItemIds = new int[]{Fang};
	}

	public static void main(String[] args)
	{
		new _00146_TheZeroHour();
	}

	@Override
	public int getQuestId()
	{
		return 146;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("31554-03.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());

		if(!st.hasQuestItems(Fang))
		{
			st.giveItems(Fang, 1);
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prev = player.getQuestState(_00109_InSearchOfTheNest.class);

		switch(st.getState())
		{
			case CREATED:
				if(player.getLevel() < 81)
				{
					return "31554-02.htm";
				}
				else
				{
					return prev != null && prev.getState() == COMPLETED ? "31554-01a.htm" : "31554-04.html";
				}
			case STARTED:
				if(st.getCond() == 1)
				{
					return "31554-06.html";
				}
				else
				{
					st.giveItems(14849, 1);
					st.addExpAndSp(154616, 12500);
					st.takeItems(Fang, 1);
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "31554-05.html";
				}
			case COMPLETED:
				return "31554-01b.htm";
		}
		return null;
	}
}
