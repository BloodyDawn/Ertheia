package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00431_WeddingMarch extends Quest
{
	// НПЦ
	private static final int MELODY_MAESTRO_KANTABILON_ID = 31042;

	// Квестовые вещи
	private static final int SILVER_CRYSTAL_ID = 7540;
	private static final int WEDDING_ECHO_CRYSTAL_ID = 7062;

	public _00431_WeddingMarch()
	{
		addStartNpc(MELODY_MAESTRO_KANTABILON_ID);
		addTalkId(MELODY_MAESTRO_KANTABILON_ID);
		addKillId(20786);
		addKillId(20787);

		questItemIds = new int[]{SILVER_CRYSTAL_ID};
	}

	public static void main(String[] args)
	{
		new _00431_WeddingMarch();
	}

	@Override
	public int getQuestId()
	{
		return 431;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return event;
		}
		int cond = st.getCond();

		if(event.equalsIgnoreCase("1") && cond == 0)
		{
			event = "31042-02.htm";
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("3") && st.getQuestItemsCount(SILVER_CRYSTAL_ID) == 50)
		{
			st.giveItems(WEDDING_ECHO_CRYSTAL_ID, 25);
			st.takeItems(SILVER_CRYSTAL_ID, 50);
			event = "31042-05.htm";
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if(partyMember == null)
		{
			return null;
		}

		QuestState st = partyMember.getQuestState(getClass());
		int count = (int) st.getQuestItemsCount(SILVER_CRYSTAL_ID);
		int i0 = Rnd.get(1000);
		if(i0 < 500 && st.getCond() == 1 && count < 50)
		{
			st.giveItems(SILVER_CRYSTAL_ID, (int) Config.RATE_QUEST_DROP);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if(count >= 50)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(player.getLevel() >= 38)
		{
			QuestStateType id = st.getState();
			int cond = st.getCond();
			if(id == CREATED)
			{
				return "31042-01.htm";
			}
			else if(cond == 1)
			{
				return "31042-03.htm";
			}
			else if(cond == 2)
			{
				return "31042-04.htm";
			}
		}
		else
		{
			return "31042-00.htm";
		}
		return null;
	}
}