package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.CompetitionType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.10.12
 * Time: 16:20
 */

public class _00553_OlympiadUndefeated extends Quest
{
	private static final int MANAGER = 31688;

	private static final int WIN_CONF_2 = 17244;
	private static final int WIN_CONF_5 = 17245;
	private static final int WIN_CONF_10 = 17246;

	private static final int OLY_CHEST = 32263;

	private static final int GIANT_FORCE = 35563;

	public _00553_OlympiadUndefeated()
	{
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		questItemIds = new int[]{WIN_CONF_2, WIN_CONF_5, WIN_CONF_10};
		addEventId(HookType.ON_OLY_BATTLE_END);
	}

	public static void main(String[] args)
	{
		new _00553_OlympiadUndefeated();
	}

	@Override
	public int getQuestId()
	{
		return 553;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "olympiad_operator_q0553_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(reply == 1)
		{
			return "olympiad_operator_q0553_02.htm";
		}
		if(reply == 10)
		{
			if(st.hasQuestItems(WIN_CONF_2))
			{
				st.giveItems(OLY_CHEST, 1);
			}
			else if(st.hasQuestItems(WIN_CONF_5))
			{
				st.giveItems(OLY_CHEST, 3);
				st.giveItems(GIANT_FORCE, 1);
				player.setFame(player.getFame() + 10000);
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.DAILY);
			return "olympiad_operator_q0553_08.htm";
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		switch(st.getState())
		{
			case COMPLETED:
				return "olympiad_operator_q0553_03a.htm";
			case CREATED:
				return player.getLevel() < 85 || !player.isNoble() || !player.isAwakened() ? "olympiad_operator_q0553_03b.htm" : "olympiad_operator_q0553_01.htm";
			case STARTED:
				if(st.getCond() == 1)
				{
					return !st.hasQuestItems(WIN_CONF_2) && !st.hasQuestItems(WIN_CONF_5) && !st.hasQuestItems(WIN_CONF_10) ? "olympiad_operator_q0553_04.htm" : "olympiad_operator_q0553_05.htm";
				}
				else if(st.getCond() == 2)
				{
					if(st.hasQuestItems(WIN_CONF_10))
					{
						st.giveItems(OLY_CHEST, 62);
						st.giveItems(GIANT_FORCE, 3);
						player.setFame(player.getFame() + 20000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "olympiad_operator_q0553_07.htm";
					}
				}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public void onOlympiadBattleEnd(L2PcInstance player, CompetitionType type, boolean isWinner)
	{
		if(player != null)
		{
			if(isWinner)
			{
				QuestState st = player.getQuestState(getClass());
				if(st != null && st.isStarted() && st.getCond() == 1)
				{
					int matches = st.getInt("undefeatable") + 1;
					switch(matches)
					{
						case 2:
							if(!st.hasQuestItems(WIN_CONF_2))
							{
								st.giveItems(WIN_CONF_2, 1);
							}
							break;
						case 5:
							if(!st.hasQuestItems(WIN_CONF_5))
							{
								st.takeItems(WIN_CONF_2, -1);
								st.giveItems(WIN_CONF_5, 1);
							}
							break;
						case 10:
							if(!st.hasQuestItems(WIN_CONF_10))
							{
								st.takeItems(WIN_CONF_5, -1);
								st.giveItems(WIN_CONF_10, 1);
								st.setCond(2);
							}
							break;
					}
					st.set("undefeatable", String.valueOf(matches));
				}
			}
			else
			{
				QuestState st = player.getQuestState(getClass());
				if(st != null && st.isStarted() && st.getCond() == 1)
				{
					st.unset("undefeatable");
					st.takeItems(WIN_CONF_2, -1);
					st.takeItems(WIN_CONF_5, -1);
					st.takeItems(WIN_CONF_10, -1);
				}
			}
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85 && player.isNoble() && player.isAwakened();

	}
}