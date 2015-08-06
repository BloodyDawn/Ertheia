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
 * Date: 18.03.13
 * Time: 19:16
 */

public class _00903_TheCallOfAntharas extends Quest
{
	// Квестовые персонажи
	private static final int Theodoric = 30755;

	// Квестовые монстры
	private static final int Tarask_Dragon = 29190;
	private static final int Behemoth_Dragon = 29069;

	// Квестовые предметы
	private static final int PortalStone = 3865;
	private static final int Reward = 21897;
	private static final int Tarask_Dragons_Leather_Fragment = 21991;
	private static final int Behemoth_Dragon_Leather = 21992;

	public _00903_TheCallOfAntharas()
	{
		addStartNpc(Theodoric);
		addTalkId(Theodoric);
		addKillId(Tarask_Dragon);
		addKillId(Behemoth_Dragon);

		questItemIds = new int[]{Tarask_Dragons_Leather_Fragment, Behemoth_Dragon_Leather};
	}

	public static void main(String[] args)
	{
		new _00903_TheCallOfAntharas();
	}

	@Override
	public int getQuestId()
	{
		return 903;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "watcher_antaras_theodric_q0903_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(reply == 1)
		{
			return "watcher_antaras_theodric_q0903_05.htm";
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		executeForEachPlayer(killer, npc, isPet, true, true);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Theodoric)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "watcher_antaras_theodric_q0903_02.htm";
				case CREATED:
					if(player.getLevel() >= 83)
					{
						if(st.hasQuestItems(PortalStone))
						{
							return "watcher_antaras_theodric_q0903_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "watcher_antaras_theodric_q0903_04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "watcher_antaras_theodric_q0903_03.htm";
					}
				case STARTED:
					if(st.getCond() == 2)
					{
						st.takeItems(Behemoth_Dragon_Leather, 1);
						st.takeItems(Tarask_Dragons_Leather_Fragment, 1);
						st.giveItems(Reward, 1);
						st.unset("cond");
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "watcher_antaras_theodric_q0903_08.htm";
					}
					else
					{
						return "watcher_antaras_theodric_q0903_07.htm";
					}
			}
		}
		return null;
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && player.isInsideRadius(npc, 2000, false, false))
		{
			if(st.getCond() == 1)
			{
				if(npc.getNpcId() == Tarask_Dragon)
				{
					st.giveItems(Tarask_Dragons_Leather_Fragment, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else if(npc.getNpcId() == Behemoth_Dragon)
				{
					st.giveItems(Behemoth_Dragon_Leather, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}

				if(st.hasQuestItems(Tarask_Dragons_Leather_Fragment) && st.hasQuestItems(Behemoth_Dragon_Leather))
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
	}
}