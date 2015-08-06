package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10268_ToTheSeedOfInfinity extends Quest
{
	// Квестовые персонажи
	private static final int KEUCEREUS = 32548;
	private static final int TEPIOS = 32603;

	// Квестовые предметы
	private static final int INTRODUCTION = 13811;

	public _10268_ToTheSeedOfInfinity()
	{
		addStartNpc(KEUCEREUS);
		addTalkId(KEUCEREUS, TEPIOS);
		questItemIds = new int[]{INTRODUCTION};
	}

	public static void main(String[] args)
	{
		new _10268_ToTheSeedOfInfinity();
	}

	@Override
	public int getQuestId()
	{
		return 10268;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(st == null)
		{
			return null;
		}
		if(event.equals("quest_accept") && !st.isStarted())
		{
			st.startQuest();
			st.giveItems(INTRODUCTION, 1);
			return "kserth_q10268_07.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		switch(npc.getNpcId())
		{
			case KEUCEREUS:
				switch(reply)
				{
					case 1:
						return "kserth_q10268_04.htm";
					case 2:
						return "kserth_q10268_05.htm";
					case 3:
						return "kserth_q10268_06.htm";
				}
				break;
		}
		return null;
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

		if(st.isCompleted())
		{
			htmltext = npc.getNpcId() == TEPIOS ? "officer_tepios_q10268_02.htm" : "kserth_q10268_03.htm";
		}
		else if(st.getState() == CREATED && npc.getNpcId() == KEUCEREUS)
		{
			htmltext = player.getLevel() < 75 ? "kserth_q10268_02.htm" : "kserth_q10268_01.htm";
		}
		else if(st.getState() == STARTED && npc.getNpcId() == KEUCEREUS)
		{
			htmltext = "kserth_q10268_08.htm";
		}
		else if(st.getState() == STARTED && npc.getNpcId() == TEPIOS)
		{
			htmltext = "officer_tepios_q10268_01.htm";
			st.giveAdena(425500, true);
			st.addExpAndSp(4000000, 4425000);
			st.unset("cond");
			st.exitQuest(QuestType.ONE_TIME);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return htmltext;
	}
}
