package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

public class _10274_CollectingInTheAir extends Quest
{
	// НПЦшки
	private static final int Lekon = 32557;

	// Квестовые итемы
	private static final int Scroll = 13844;
	private static final int red = 13858;
	private static final int blue = 13859;
	private static final int green = 13860;

	public _10274_CollectingInTheAir()
	{
		addStartNpc(Lekon);
		addTalkId(Lekon);
		questItemIds = new int[]{Scroll, red, blue, green};
	}

	public static void main(String[] args)
	{
		new _10274_CollectingInTheAir();
	}

	@Override
	public int getQuestId()
	{
		return 10274;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(st == null)
		{
			return null;
		}
		if(event.equals("32557-03.htm"))
		{
			st.giveItems(Scroll, 8);
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return htmltext;
		}

		QuestStateType id = st.getState();

		switch(id)
		{
			case COMPLETED:
				htmltext = "32557-0a.htm";
				break;
			case CREATED:
				QuestState qs = player.getQuestState(_10273_GoodDayToFly.class);
				if(qs != null)
				{
					htmltext = qs.getState() == COMPLETED && player.getLevel() >= 75 ? "32557-01.htm" : "32557-00.htm";
				}
				break;
			case STARTED:
				if(st.getQuestItemsCount(red) + st.getQuestItemsCount(blue) + st.getQuestItemsCount(green) >= 8)
				{
					htmltext = "32557-05.htm";
					st.giveItems(13728, 1);
					st.addExpAndSp(25160, 2525);
					st.unset("transform");
					st.unset("cond");
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				}
				else
				{
					htmltext = "32557-04.htm";
				}
				break;
		}
		return htmltext;
	}
}
