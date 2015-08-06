package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

public class _00042_HelpTheUncle extends Quest
{
	private static final int WATERS = 30828;
	private static final int SOPHYA = 30735;

	private static final int TRIDENT = 291;
	private static final int MAP_PIECE = 7548;
	private static final int MAP = 7549;
	private static final int PET_TICKET = 7583;

	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MONSTER_EYE_GAZER = 20266;

	private static final int MAX_COUNT = 30;

	public _00042_HelpTheUncle()
	{
		addStartNpc(WATERS);
		addTalkId(WATERS, SOPHYA);
		addKillId(MONSTER_EYE_DESTROYER, MONSTER_EYE_GAZER);
	}

	public static void main(String[] args)
	{
		new _00042_HelpTheUncle();
	}

	@Override
	public int getQuestId()
	{
		return 42;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "30828-01.htm";
			st.startQuest();
		}
		else if(event.equals("3") && st.hasQuestItems(TRIDENT))
		{
			htmltext = "30828-03.htm";
			st.takeItems(TRIDENT, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "30828-05.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.hasQuestItems(MAP))
		{
			htmltext = "30735-06.htm";
			st.takeItems(MAP, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "30828-07.htm";
			st.giveItems(PET_TICKET, 1);
			st.unset("cond");
			st.exitQuest(QuestType.ONE_TIME);
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2)
		{
			long pieces = st.getQuestItemsCount(MAP_PIECE);
			if(pieces < MAX_COUNT - 1)
			{
				st.giveItems(MAP_PIECE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if(pieces == MAX_COUNT - 1)
			{
				st.giveItems(MAP_PIECE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(3);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		QuestStateType id = st.getState();
		int cond = st.getCond();
		if(id == CREATED)
		{
			if(st.getPlayer().getLevel() >= 25)
			{
				htmltext = "30828-00.htm";
			}
			else
			{
				htmltext = getLowLevelMsg(25);
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		else if(id == STARTED)
		{
			if(npcId == WATERS)
			{
				switch(cond)
				{
					case 1:
						htmltext = !st.hasQuestItems(TRIDENT) ? "30828-01a.htm" : "30828-02.htm";
						break;
					case 2:
						htmltext = "30828-03a.htm";
						break;
					case 3:
						htmltext = "30828-04.htm";
						break;
					case 4:
						htmltext = "30828-05a.htm";
						break;
					case 5:
						htmltext = "30828-06.htm";
						break;
				}
			}
			else if(npcId == SOPHYA)
			{
				if(cond == 4 && st.hasQuestItems(MAP))
				{
					htmltext = "30735-05.htm";
				}
				else if(cond == 5)
				{
					htmltext = "30735-06a.htm";
				}
			}
		}
		return htmltext;
	}
}