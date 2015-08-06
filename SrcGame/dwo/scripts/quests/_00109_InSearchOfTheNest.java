package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

public class _00109_InSearchOfTheNest extends Quest
{
	// НПЦшки
	private static final int PIERCE = 31553;
	private static final int CORPSE = 32015;
	private static final int KAHMAN = 31554;

	// Квестовые предметы
	private static final int MEMO = 8083;
	private static final int GOLDEN_BADGE_RECRUIT = 7246;
	private static final int GOLDEN_BADGE_SOLDIER = 7247;

	public _00109_InSearchOfTheNest()
	{
		addStartNpc(PIERCE);
		addTalkId(PIERCE, CORPSE, KAHMAN);
		questItemIds = new int[]{MEMO};
	}

	public static void main(String[] args)
	{
		new _00109_InSearchOfTheNest();
	}

	@Override
	public int getQuestId()
	{
		return 109;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		int cond = st.getCond();
		if(event.equalsIgnoreCase("Memo") && cond == 1)
		{
			st.giveItems(MEMO, 1);
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			htmltext = "32015-03.htm";
		}
		else if(event.equalsIgnoreCase("31553-02.htm") && cond == 2)
		{
			st.takeItems(MEMO, -1);
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		QuestStateType id = st.getState();
		if(id == COMPLETED)
		{
			return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		int cond = st.getCond();
		String htmltext = getNoQuestMsg(st.getPlayer());

		if(id == CREATED)
		{
			if(st.getPlayer().getLevel() >= 81 && npcId == PIERCE && st.hasQuestItems(GOLDEN_BADGE_RECRUIT) || st.hasQuestItems(GOLDEN_BADGE_SOLDIER))
			{
				st.startQuest();
				htmltext = "31553-03.htm";
			}
			else
			{
				htmltext = "31553-00.htm";
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		else if(id == STARTED)
		{
			if(npcId == CORPSE)
			{
				if(cond == 1)
				{
					htmltext = "32015-01.htm";
				}
				else if(cond == 2)
				{
					htmltext = "32015-02.htm";
				}
			}
			else if(npcId == PIERCE)
			{
				if(cond == 1)
				{
					htmltext = "31553-04.htm";
				}
				else if(cond == 2)
				{
					htmltext = "31553-01.htm";
				}
				else if(cond == 3)
				{
					htmltext = "31553-05.htm";
				}
			}
			else if(npcId == KAHMAN && cond == 3)
			{
				htmltext = "31554-01.htm";
				st.addExpAndSp(701500, 50000);
				st.giveAdena(161500, true);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			}
		}
		return htmltext;
	}
}