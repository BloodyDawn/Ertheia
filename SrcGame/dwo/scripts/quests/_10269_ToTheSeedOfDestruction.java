package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

public class _10269_ToTheSeedOfDestruction extends Quest
{
	// Квестовые персонажи
	private static final int KEUCEREUS = 32548;
	private static final int ALLENOS = 32526;

	// Квестовые предметы
	private static final int INTRODUCTION = 13812;

	public _10269_ToTheSeedOfDestruction()
	{

		addStartNpc(KEUCEREUS);
		addTalkId(KEUCEREUS, ALLENOS);
	}

	public static void main(String[] args)
	{
		new _10269_ToTheSeedOfDestruction();
	}

	@Override
	public int getQuestId()
	{
		return 10269;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("quest_accept") && !st.isStarted())
		{
			st.startQuest();
			st.giveItems(INTRODUCTION, 1);
			return "kserth_q10269_07.htm";
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
						return "kserth_q10269_04.htm";
					case 2:
						return "kserth_q10269_05.htm";
					case 3:
						return "kserth_q10269_06.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		QuestStateType id = st.getState();
		int npcId = npc.getNpcId();
		if(id == COMPLETED)
		{
			return npcId == ALLENOS ? "servant_of_kserth_q10269_02.htm" : "kserth_q10269_03.htm";
		}
		if(id == CREATED && npcId == KEUCEREUS)
		{
			return st.getPlayer().getLevel() < 75 ? "kserth_q10269_02.htm" : "kserth_q10269_01.htm";
		}
		if(id == STARTED && npcId == KEUCEREUS)
		{
			return "kserth_q10269_08.htm";
		}
		if(id == STARTED && npcId == ALLENOS)
		{
			st.giveAdena(710000, true);
			st.addExpAndSp(6660000, 7375000);
			st.exitQuest(QuestType.ONE_TIME);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			return "servant_of_kserth_q10269_01.htm";
		}
		return null;
	}
}