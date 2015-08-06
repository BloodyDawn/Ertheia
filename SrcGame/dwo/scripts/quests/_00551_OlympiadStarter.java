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
 * Time: 15:04
 */

public class _00551_OlympiadStarter extends Quest
{
	private static final int MANAGER = 31688;

	private static final int CERT_3 = 17238;
	private static final int CERT_5 = 17239;
	private static final int CERT_10 = 17240;

	private static final int OLY_CHEST = 32263;

	private static final int GIANT_FORCE = 35563;

	public _00551_OlympiadStarter()
	{
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		questItemIds = new int[]{CERT_3, CERT_5, CERT_10};
		addEventId(HookType.ON_OLY_BATTLE_END);
	}

	public static void main(String[] args)
	{
		new _00551_OlympiadStarter();
	}

	@Override
	public int getQuestId()
	{
		return 551;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "olympiad_operator_q0551_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(reply == 1)
		{
			return "olympiad_operator_q0551_02.htm";
		}
		if(reply == 10)
		{
			if(st.hasQuestItems(CERT_3))
			{
				st.giveItems(OLY_CHEST, 1);
			}
			else if(st.hasQuestItems(CERT_5))
			{
				st.giveItems(OLY_CHEST, 2);
				st.giveItems(GIANT_FORCE, 1);
				player.setFame(player.getFame() + 6000);
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.DAILY);
			return "olympiad_operator_q0551_08.htm";
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
				return "olympiad_operator_q0551_03a.htm";
			case CREATED:
				return player.getLevel() < 85 || !player.isNoble() || !player.isAwakened() ? "olympiad_operator_q0551_03b.htm" : "olympiad_operator_q0551_01.htm";
			case STARTED:
				if(st.getCond() == 1)
				{
					return !st.hasQuestItems(CERT_3) && !st.hasQuestItems(CERT_5) && !st.hasQuestItems(CERT_10) ? "olympiad_operator_q0551_04.htm" : "olympiad_operator_q0551_05.htm";
				}
				else if(st.getCond() == 2)
				{
					if(st.hasQuestItems(CERT_10))
					{
						st.giveItems(OLY_CHEST, 2);
						st.giveItems(GIANT_FORCE, 2);
						player.setFame(player.getFame() + 10000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "olympiad_operator_q0551_07.htm";
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
			QuestState st = player.getQuestState(getClass());
			if(st != null && st.getCond() == 1)
			{
				int matches = st.getInt("matches") + 1;
				switch(matches)
				{
					case 3:
						if(!st.hasQuestItems(CERT_3))
						{
							st.giveItems(CERT_3, 1);
						}
						break;
					case 5:
						if(!st.hasQuestItems(CERT_5))
						{
							st.takeItems(CERT_3, -1);
							st.giveItems(CERT_5, 1);
						}
						break;
					case 10:
						if(!st.hasQuestItems(CERT_10))
						{
							st.takeItems(CERT_5, -1);
							st.giveItems(CERT_10, 1);
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
				}
				st.set("matches", String.valueOf(matches));
			}
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85 && player.isNoble() && player.isAwakened();

	}
}