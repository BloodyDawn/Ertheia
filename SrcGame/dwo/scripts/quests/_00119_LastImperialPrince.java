package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.08.11
 * Time: 13:42
 */

public class _00119_LastImperialPrince extends Quest
{
	// Квестовые персонажи
	private static final int _SPIRIT = 31453;
	private static final int _DEVORIN = 32009;

	// Квестовые предметы
	private static final int _BROOCH = 7262;

	public _00119_LastImperialPrince()
	{
		addStartNpc(_SPIRIT);
		addTalkId(_SPIRIT, _DEVORIN);
		addKillId(22023, 22024);
		questItemIds = new int[]{_BROOCH};
	}

	public static void main(String[] args)
	{
		new _00119_LastImperialPrince();
	}

	@Override
	public int getQuestId()
	{
		return 119;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return event;
		}
		if(event.equalsIgnoreCase("31453-4.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("32009-2.htm"))
		{
			if(!st.hasQuestItems(_BROOCH))
			{
				event = "<html><body>Для выполнения этого задания требуется закончить квест <font color=\"LEVEL\">Четыре Таблицы</font>.</body></html>";
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		else if(event.equalsIgnoreCase("32009-3.htm"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31453-7.htm"))
		{
			st.giveAdena(150292, true);
			st.addExpAndSp(902439, 90067);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(!st.hasQuestItems(_BROOCH))
		{
			st.exitQuest(QuestType.REPEATABLE);
			return "<html><body>Для выполнения этого задания требуется закончить квест <font color=\"LEVEL\">Четыре Таблицы</font>.</body></html>";
		}
		if(st.getState() == CREATED)
		{
			if(player.getLevel() < 74)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "<html><body>Квест предназначен для персонажей 74 уровня и выше.</body></html>";
			}
			else
			{
				return "31453-1.htm";
			}
		}
		if(st.getState() == COMPLETED)
		{
			st.exitQuest(QuestType.REPEATABLE);
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		if(npcId == _SPIRIT)
		{
			if(cond == 1)
			{
				return "31453-4.htm";
			}
			else if(cond == 2)
			{
				return "31453-5.htm";
			}
		}
		else if(npcId == _DEVORIN)
		{
			if(cond == 1)
			{
				return "32009-1.htm";
			}
			else if(cond == 2)
			{
				return "32009-3.htm";
			}
		}
		return null;
	}
}