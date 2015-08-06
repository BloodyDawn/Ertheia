package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom extends Quest
{
	// NPC
	private static final int Sophia1 = 32596;
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32785;
	private static final int Books = 32809;
	private static final int Books1 = 32810;
	private static final int Books2 = 32811;
	private static final int Books3 = 32812;
	private static final int Books4 = 32813;
	private static final int Sophia2 = 32861;
	private static final int Sophia3 = 32863;
	// Item
	private static final int SolinasBiography = 17213;

	public _10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom()
	{
		addStartNpc(Elcadia);
		addTalkId(Elcadia);
		addTalkId(Sophia1);
		addTalkId(Elcadia_Support);
		addTalkId(Books);
		addTalkId(Books1);
		addTalkId(Books2);
		addTalkId(Books3);
		addTalkId(Books4);
		addTalkId(Sophia2);
		addTalkId(Sophia3);
		addStartNpc(Sophia3);
		addFirstTalkId(Sophia3);
		questItemIds = new int[]{SolinasBiography};
	}

	public static void main(String[] args)
	{
		new _10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom();
	}

	@Override
	public int getQuestId()
	{
		return 10293;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == Elcadia)
		{
			if(event.equalsIgnoreCase("32784-04.html"))
			{
				st.startQuest();
			}
			else if(event.equalsIgnoreCase("32784-09.html"))
			{
				if(player.isSubClassActive())
				{
					return "32784-10.html";
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.addExpAndSp(15000000, 1500000);
					st.exitQuest(QuestType.ONE_TIME);
					return "32784-09.html";
				}
			}
		}
		else if(npc.getNpcId() == Sophia2)
		{
			if(event.equalsIgnoreCase("32861-04.html"))
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			if(event.equalsIgnoreCase("32861-08.html"))
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			if(event.equalsIgnoreCase("32861-11.html"))
			{
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			if(event.equalsIgnoreCase("32785-07.html"))
			{
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == Books)
		{
			if(event.equalsIgnoreCase("32809-02.html"))
			{
				st.setCond(7);
				st.giveItems(SolinasBiography, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Elcadia)
		{
			if(st.isCompleted())
			{
				return "32784-02.html";
			}
			else if(player.getLevel() < 81)
			{
				return "32784-11.htm";
			}
			else if(player.getQuestState(_10292_SevenSignsGirlofDoubt.class) == null || !player.getQuestState(_10292_SevenSignsGirlofDoubt.class).isCompleted())
			{
				return "32784-11.htm";
			}
			else if(st.isCreated())
			{
				return "32784-01.htm";
			}
			else if(st.getCond() == 1)
			{
				return "32784-06.html";
			}
			else if(st.getCond() >= 8)
			{
				return "32784-07.html";
			}
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			switch(st.getCond())
			{
				case 1:
					return "32785-01.html";
				case 2:
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32785-04.html";
				case 3:
					return "32785-05.html";
				case 4:
					return "32785-06.html";
				case 5:
					return "32785-08.html";
				case 6:
					return "32785-09.html";
				case 7:
					st.setCond(8);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32785-11.html";
				case 8:
					return "32785-12.html";
			}
		}
		else if(npc.getNpcId() == Sophia1)
		{
			switch(st.getCond())
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					return "32596-01.html";
				case 8:
					return "32596-05.html";
			}
		}
		else if(npc.getNpcId() == Sophia2)
		{
			switch(st.getCond())
			{
				case 1:
					return "32861-01.html";
				case 2:
					return "32861-05.html";
				case 3:
					return "32861-06.html";
				case 4:
					return "32861-09.html";
				case 5:
					return "32861-10.html";
				case 6:
				case 7:
					return "32861-12.html";
				case 8:
					return "32861-14.html";
			}
		}
		else if(npc.getNpcId() == Books)
		{
			if(st.getCond() == 6)
			{
				return "32809-01.html";
			}
		}
		else if(npc.getNpcId() == Books1)
		{
			if(st.getCond() == 6)
			{
				return "32810-01.html";
			}
		}
		else if(npc.getNpcId() == Books2)
		{
			if(st.getCond() == 6)
			{
				return "32811-01.html";
			}
		}
		else if(npc.getNpcId() == Books3)
		{
			if(st.getCond() == 6)
			{
				return "32812-01.html";
			}
		}
		else if(npc.getNpcId() == Books4)
		{
			if(st.getCond() == 6)
			{
				return "32813-01.html";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(npc.getNpcId() == Sophia3)
		{
			switch(st.getCond())
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					return "32863-01.html";
				case 8:
					return "32863-04.html";
			}
		}
		return null;
	}
}