package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.03.13
 * Time: 14:21
 */

public class _10502_FreyaEmbroideredSoulCloak extends Quest
{
	// Квестовые персонажи
	private static final int OlfAdams = 32612;

	// Квестовые монстры
	private static final int Freya = 29179; // 29177

	// Квестовые предметы
	private static final int FreyaSoulFragment = 21723;

	// Награда
	private static final int CloakofFreya = 21714;

	public _10502_FreyaEmbroideredSoulCloak()
	{
		addStartNpc(OlfAdams);
		addTalkId(OlfAdams);
		addKillId(Freya);
		questItemIds = new int[]{FreyaSoulFragment};
	}

	public static void main(String[] args)
	{
		new _10502_FreyaEmbroideredSoulCloak();
	}

	@Override
	public int getQuestId()
	{
		return 10502;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(st != null && event.equals("quest_accept") && !st.isCompleted())
		{
			if(!st.isStarted())
			{
				st.startQuest();
			}
			return "weaver_wolf_adams_q10502_04.htm";
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, true);
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		switch(st.getState())
		{
			case COMPLETED:
				return "weaver_wolf_adams_q10502_03.htm";
			case CREATED:
				if(player.getLevel() < 80)
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "weaver_wolf_adams_q10502_02.htm";
				}
				else
				{
					return "weaver_wolf_adams_q10502_01.htm";
				}
			case STARTED:
				if(st.getCond() == 1)
				{
					return "weaver_wolf_adams_q10502_05.htm";
				}
				if(st.getCond() == 2)
				{
					st.giveItems(CloakofFreya, 1);
					st.takeItems(FreyaSoulFragment, -1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "weaver_wolf_adams_q10502_06.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 80;
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1 && Util.checkIfInRange(1500, npc, player, false))
		{
			long currentCount = st.getQuestItemsCount(FreyaSoulFragment);
			long count = Rnd.get(1, 3);
			if(count > 20 - currentCount)
			{
				st.giveItems(FreyaSoulFragment, 20 - currentCount);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.giveItems(FreyaSoulFragment, count);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
	}
}