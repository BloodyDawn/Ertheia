package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

public class _00044_HelpTheSon extends Quest
{
	private static final int LUNDY = 30827;
	private static final int DRIKUS = 30505;

	private static final int WORK_HAMMER = 168;
	private static final int GEMSTONE_FRAGMENT = 7552;
	private static final int GEMSTONE = 7553;
	private static final int PET_TICKET = 7585;

	private static final int MAILLE_GUARD = 20921;
	private static final int MAILLE_SCOUT = 20920;
	private static final int MAILLE_LIZARDMAN = 20919;

	public _00044_HelpTheSon()
	{
		addStartNpc(LUNDY);
		addTalkId(LUNDY, DRIKUS);
		addKillId(MAILLE_GUARD, MAILLE_SCOUT, MAILLE_LIZARDMAN);
		questItemIds = new int[]{GEMSTONE_FRAGMENT};
	}

	public static void main(String[] args)
	{
		new _00044_HelpTheSon();
	}

	@Override
	public int getQuestId()
	{
		return 44;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "30827-01.htm";
			st.startQuest();
		}
		else if(event.equals("3") && st.hasQuestItems(WORK_HAMMER))
		{
			htmltext = "30827-03.htm";
			st.takeItems(WORK_HAMMER, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(GEMSTONE_FRAGMENT) >= 30)
		{
			htmltext = "30827-05.htm";
			st.takeItems(GEMSTONE_FRAGMENT, -1);
			st.giveItems(GEMSTONE, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.hasQuestItems(GEMSTONE))
		{
			htmltext = "30505-06.htm";
			st.takeItems(GEMSTONE, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "30827-07.htm";
			st.giveItems(PET_TICKET, 1);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(st.getCond() == 2 && st.getQuestItemsCount(GEMSTONE_FRAGMENT) < 30)
		{
			st.giveItems(GEMSTONE_FRAGMENT, 1);
			if(st.getQuestItemsCount(GEMSTONE_FRAGMENT) >= 30)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = getNoQuestMsg(st.getPlayer());
		QuestStateType id = st.getState();
		if(id == CREATED)
		{
			if(st.getPlayer().getLevel() >= 24)
			{
				htmltext = "30827-00.htm";
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				htmltext = "30827-000.htm";
			}
		}
		else if(id == STARTED)
		{
			if(npcId == LUNDY)
			{
				switch(st.getCond())
				{
					case 1:
						htmltext = st.getQuestItemsCount(WORK_HAMMER) == 0 ? "30827-01a.htm" : "30827-02.htm";
						break;
					case 2:
						htmltext = "30827-03a.htm";
						break;
					case 3:
						htmltext = "30827-04.htm";
						break;
					case 4:
						htmltext = "30827-05a.htm";
						break;
					case 5:
						htmltext = "30827-06.htm";
						break;
				}
			}
			else if(npcId == DRIKUS)
			{
				if(st.getCond() == 4 && st.getQuestItemsCount(GEMSTONE) > 0)
				{
					htmltext = "30505-05.htm";
				}
				else if(st.getCond() == 5)
				{
					htmltext = "30505-06a.htm";
				}
			}
		}
		return htmltext;
	}
}