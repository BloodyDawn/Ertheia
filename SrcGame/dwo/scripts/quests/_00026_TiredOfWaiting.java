package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00026_TiredOfWaiting extends Quest
{
	// Квестовые персонажи
	private static final int ISAEL_SILVERSHADOW = 30655;
	private static final int KITZKA = 31045;

	// Квестовые предметы
	private static final int DELIVERY_BOX = 17281;
	private static final int WILL_OF_ANTHARAS = 17266;
	private static final int LARGE_DRAGON_BONE = 17248;
	private static final int SEALED_BLOOD_CRYSTAL = 17267;

	public _00026_TiredOfWaiting()
	{
		addStartNpc(ISAEL_SILVERSHADOW);
		addTalkId(ISAEL_SILVERSHADOW, KITZKA);
		questItemIds = new int[]{DELIVERY_BOX};
	}

	public static void main(String[] args)
	{
		new _00026_TiredOfWaiting();
	}

	@Override
	public int getQuestId()
	{
		return 26;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return event;
		}

		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case ISAEL_SILVERSHADOW:
				if(event.equalsIgnoreCase("30655-04.html"))
				{
					st.startQuest();
					st.giveItems(DELIVERY_BOX, 1);
				}
				break;
			case KITZKA:
				if(event.equalsIgnoreCase("31045-04.html"))
				{
					st.takeItems(DELIVERY_BOX, 1);
				}
				else if(event.equalsIgnoreCase("31045-10.html"))
				{
					st.giveItems(LARGE_DRAGON_BONE, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else if(event.equalsIgnoreCase("31045-11.html"))
				{
					st.giveItems(WILL_OF_ANTHARAS, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else if(event.equalsIgnoreCase("31045-12.html"))
				{
					st.giveItems(SEALED_BLOOD_CRYSTAL, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int npcId = npc.getNpcId();
		switch(st.getState())
		{
			case CREATED:
				if(npcId == ISAEL_SILVERSHADOW)
				{
					return player.getLevel() >= 80 ? "30655-01.htm" : "30655-00.html";
				}
				break;
			case STARTED:
				if(st.getCond() == 1)
				{
					switch(npcId)
					{
						case ISAEL_SILVERSHADOW:
							return "30655-07.html";
						case KITZKA:
							return st.hasQuestItems(DELIVERY_BOX) ? "31045-01.html" : "31045-09.html";
					}
				}
				break;
			case COMPLETED:
				if(npcId == ISAEL_SILVERSHADOW)
				{
					return "30655-08.html";
				}
				break;
		}
		return null;
	}
}