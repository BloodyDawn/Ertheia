package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.07.12
 * Time: 17:11
 */

public class _10748_MysteriousSuggestion1 extends Quest
{
	// Квестовые персонажи
	private static final int ТаинственныйЛакей = 33685;

	// Награды
	private static final int ТаинственнаяСила = 34904;
	private static final int ТаинственнаяТень = 34903;

	// Квестовые предметы
	private static final int СвидетельствоУчастия1 = 35544;

	public _10748_MysteriousSuggestion1()
	{
		addStartNpc(ТаинственныйЛакей);
		addTalkId(ТаинственныйЛакей);
		addEventId(HookType.ON_CHAOS_BATTLE_END);
		questItemIds = new int[]{СвидетельствоУчастия1};
	}

	public static void main(String[] args)
	{
		new _10748_MysteriousSuggestion1();
	}

	@Override
	public int getQuestId()
	{
		return 10748;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "grankain_lumiere_q10748_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == ТаинственныйЛакей)
		{
			if(reply == 1)
			{
				return "grankain_lumiere_q10748_02.htm";
			}
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

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == ТаинственныйЛакей)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 76 || player.getClan() == null || player.getClan().getLevel() < 3)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "grankain_lumiere_q10748_04.htm";
					}
					else
					{
						return "grankain_lumiere_q10748_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "grankain_lumiere_q10748_06.htm";
					}
					if(st.getCond() == 2)
					{
						st.giveItem(ТаинственнаяСила);
						st.giveItem(ТаинственнаяТень);
						player.setFame(player.getFame() + 2000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "grankain_lumiere_q10748_07.htm";
					}
					break;
				case COMPLETED:
					return "grankain_lumiere_q10748_05.htm";
			}
		}

		return htmltext;
	}

	@Override
	public void onChaosBattleEnd(L2PcInstance player, boolean isWinner)
	{
		if(player != null)
		{
			QuestState st = player.getQuestState(getClass());
			if(st != null && st.isStarted())
			{
				if(st.getQuestItemsCount(СвидетельствоУчастия1) < 2)
				{
					st.giveItem(СвидетельствоУчастия1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(СвидетельствоУчастия1) == 2)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 && player.getClan() != null && player.getClan().getLevel() > 3;

	}
}