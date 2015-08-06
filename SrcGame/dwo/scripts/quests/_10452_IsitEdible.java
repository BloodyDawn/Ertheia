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
 * Date: 11.06.13
 * Time: 20:26
 */

public class _10452_IsitEdible extends Quest
{
	// Квестовые персонажи
	private static final int HARRY = 32743;

	// Квестовые монстры
	private static final int FANTASY_MUSHROOM = 18864;
	private static final int STICKY_MUSHROOM = 18865;
	private static final int VITALITY_PLANT = 18868;

	//Квестовые предметы
	private static final int FANTASY_SPORE = 36688;
	private static final int STICKY_SPORE = 36689;
	private static final int LEAF_POUCH = 36690;

	public _10452_IsitEdible()
	{
		addStartNpc(HARRY);
		addTalkId(HARRY);
		addKillId(FANTASY_MUSHROOM, STICKY_MUSHROOM, VITALITY_PLANT);
		questItemIds = new int[]{FANTASY_SPORE, STICKY_SPORE, LEAF_POUCH};
	}

	public static void main(String[] args)
	{
		new _10452_IsitEdible();
	}

	@Override
	public int getQuestId()
	{
		return 10452;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "cute_harry_q10452_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == HARRY)
		{
			switch(reply)
			{
				case 1:
					return "cute_harry_q10452_03.htm";
				case 10:
					return "cute_harry_q10452_09.htm";
				case 11:
					return "cute_harry_q10452_10.htm";
				case 12:
					return "cute_harry_q10452_11.htm";
				case 13:
					if(st.getCond() == 2)
					{
						st.addExpAndSp(14120400, 141204);
						st.giveAdena(299940, true);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "cute_harry_q10452_12.htm";
					}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && st.getCond() == 1)
		{
			if(npc.getNpcId() == FANTASY_MUSHROOM)
			{
				if(!st.hasQuestItems(FANTASY_SPORE))
				{
					st.giveItem(FANTASY_SPORE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if(npc.getNpcId() == STICKY_MUSHROOM)
			{
				if(!st.hasQuestItems(STICKY_SPORE))
				{
					st.giveItem(STICKY_SPORE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if(npc.getNpcId() == VITALITY_PLANT)
			{
				if(!st.hasQuestItems(LEAF_POUCH))
				{
					st.giveItem(LEAF_POUCH);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}

			if(st.hasQuestItems(FANTASY_SPORE) && st.hasQuestItems(STICKY_SPORE) && st.hasQuestItems(VITALITY_PLANT))
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == HARRY)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					return st.getPlayer().getLevel() >= 81 ? "cute_harry_q10452_01.htm" : "cute_harry_q10452_02.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "cute_harry_q10452_06.htm";
					}
					else if(st.getCond() == 2)
					{
						if(st.getBool("talked"))
						{
							return "cute_harry_q10452_08.htm";
						}
						else
						{
							st.set("talked", "true");
							return "cute_harry_q10452_07.htm";
						}
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 81;
	}
}