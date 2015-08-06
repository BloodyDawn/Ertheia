package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00124_MeetingTheElroki extends Quest
{
	// Квестовые персонажи
	private static final int Marquez = 32113;
	private static final int Mushika = 32114;
	private static final int Asamah = 32115;
	private static final int Karakawei = 32117;
	private static final int Mantarasa = 32118;

	// Квестовые предметы
	private static final int Mushika_egg = 8778;

	public _00124_MeetingTheElroki()
	{
		addStartNpc(Marquez);
		addTalkId(Marquez, Mushika, Asamah, Karakawei, Mantarasa);
		questItemIds = new int[]{Mushika_egg};
	}

	public static void main(String[] args)
	{
		new _00124_MeetingTheElroki();
	}

	@Override
	public int getQuestId()
	{
		return 124;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		switch(event)
		{
			case "32113-02.htm":
				st.setState(STARTED);
				break;
			case "32113-04.htm":
				st.startQuest();
				break;
			case "32113-05.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32114-03.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32115-06.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32117-05.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32118-02.htm":
				st.giveItems(Mushika_egg, 1);
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Marquez)
		{
			switch(cond)
			{
				case 0:
					if(st.getPlayer().getLevel() < 75)
					{
						htmltext = "32113-02.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
					else
					{
						htmltext = "32113-01.htm";
					}
					break;
				case 1:
					htmltext = "32113-04.htm";
					break;
				case 2:
					htmltext = "32113-06.htm";
					break;
			}
		}
		else if(npcId == Mushika)
		{
			if(cond < 2)
			{
				htmltext = "32114-02.htm";
			}
			else if(cond == 2)
			{
				htmltext = "32114-01.htm";
			}
			else if(cond > 2)
			{
				htmltext = "32114-04.htm";
			}
		}
		else if(npcId == Asamah)
		{
			switch(cond)
			{
				case 1:
				case 2:
					htmltext = "32115-02.htm";
					break;
				case 3:
					htmltext = "32115-01.htm";
					break;
				case 4:
					htmltext = "32115-07.htm";
					break;
				case 6:
					htmltext = "32115-08.htm";
					st.takeItems(Mushika_egg, 1);
					st.giveAdena(236510, true);
					st.addExpAndSp(1109665, 1229015);
					st.unset("cond");
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					break;
			}
		}
		else if(npcId == Karakawei)
		{
			if(cond < 4)
			{
				htmltext = "32117-02.htm";
			}
			else if(cond == 4)
			{
				htmltext = "32117-01.htm";
			}
			else if(cond == 5)
			{
				htmltext = "32117-06.htm";
			}
		}
		else
		{
			htmltext = npcId == Mantarasa && cond == 5 ? "32118-01.htm" : getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		return htmltext;
	}
}