package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

public class _00043_HelpTheSister extends Quest
{
	// Квестовые персонажи
	private static final int COOPER = 30829;
	private static final int GALLADUCCI = 30097;

	// Квестовые предметы
	private static final int CRAFTED_DAGGER = 220;
	private static final int MAP_PIECE = 7550;
	private static final int MAP = 7551;
	private static final int PET_TICKET = 7584;

	// Квестовые монстры
	private static final int[] Монстры = {20147, 20203, 20205, 20224, 20265, 20266, 20291, 20292};

	// Разное
	private static final int MAX_COUNT = 30;

	public _00043_HelpTheSister()
	{
		addStartNpc(COOPER);
		addTalkId(COOPER, GALLADUCCI);
		addKillId(Монстры);
	}

	public static void main(String[] args)
	{
		new _00043_HelpTheSister();
	}

	@Override
	public int getQuestId()
	{
		return 43;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "30829-01.htm";
			st.startQuest();
		}
		else if(event.equals("3") && st.hasQuestItems(CRAFTED_DAGGER))
		{
			htmltext = "30829-03.htm";
			st.takeItems(CRAFTED_DAGGER, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "30829-05.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.hasQuestItems(MAP))
		{
			htmltext = "30097-06.htm";
			st.takeItems(MAP, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "30829-07.htm";
			st.giveItems(PET_TICKET, 1);
			st.setCond(0);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2 && ArrayUtils.contains(Монстры, npc.getNpcId()))
		{
			long pieces = st.getQuestItemsCount(MAP_PIECE);
			if(pieces < MAX_COUNT)
			{
				st.giveItems(MAP_PIECE, 1);
				if(pieces < MAX_COUNT - 1)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(3);
				}
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
			if(st.getPlayer().getLevel() >= 26)
			{
				htmltext = "30829-00.htm";
			}
			else
			{
				htmltext = getLowLevelMsg(26);
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		else if(id == STARTED)
		{
			int cond = st.getCond();
			if(npcId == COOPER)
			{
				switch(cond)
				{
					case 1:
						htmltext = !st.hasQuestItems(CRAFTED_DAGGER) ? "30829-01a.htm" : "30829-02.htm";
						break;
					case 2:
						htmltext = "30829-03a.htm";
						break;
					case 3:
						htmltext = "30829-04.htm";
						break;
					case 4:
						htmltext = "30829-05a.htm";
						break;
					case 5:
						htmltext = "30829-06.htm";
						break;
				}
			}
			else if(npcId == GALLADUCCI)
			{
				if(cond == 4 && st.hasQuestItems(MAP))
				{
					htmltext = "30097-05.htm";
				}
			}
		}
		return htmltext;
	}
}