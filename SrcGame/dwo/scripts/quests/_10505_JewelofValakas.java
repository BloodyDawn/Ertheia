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
 * Time: 14:21
 */

public class _10505_JewelofValakas extends Quest
{
	// Квестовые персонажи
	private static final int Klein = 31540;

	// Квестовые монстры
	private static final int Valakas = 32123;

	// Квестовые предметы
	private static final int FloatingStone = 7267;
	private static final int EmptyCrystal = 21906;
	private static final int FilledCrystal = 21908;
	private static final int JewelOfValakas = 21896;

	public _10505_JewelofValakas()
	{
		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Valakas);
	}

	public static void main(String[] args)
	{
		new _10505_JewelofValakas();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(player.isInsideRadius(npc, 2000, false, false))
		{
			if(st != null && st.getCond() == 1 && npc.getNpcId() == Valakas && st.hasQuestItems(EmptyCrystal))
			{
				st.takeItems(EmptyCrystal, 1);
				st.giveItems(FilledCrystal, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10505;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(st != null && event.equals("quest_accept") && !st.isCompleted())
		{
			if(!st.isStarted())
			{
				st.startQuest();
				st.giveItems(EmptyCrystal, 1);
			}
			return "watcher_valakas_klein_q10505_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Klein)
		{
			switch(reply)
			{
				case 1:
					return "watcher_valakas_klein_q10505_05.htm";
				case 2:
					return "watcher_valakas_klein_q10505_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(killer.isInParty())
		{
			for(L2PcInstance player : killer.getParty().getMembers())
			{
				rewardPlayer(npc, player);
			}
		}
		else
		{
			rewardPlayer(npc, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Klein)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 83)
					{
						if(st.hasQuestItems(FloatingStone))
						{
							return "watcher_valakas_klein_q10505_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "watcher_valakas_klein_q10505_04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "watcher_valakas_klein_q10505_02.htm";
					}
				case COMPLETED:
					return "watcher_valakas_klein_q10505_03.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						if(st.hasQuestItems(EmptyCrystal))
						{
							return "watcher_valakas_klein_q10505_08.htm";
						}
						else
						{
							st.giveItems(EmptyCrystal, 1);
							return "watcher_valakas_klein_q10505_09.htm";
						}
					}
					if(st.getCond() == 2 && st.hasQuestItems(FilledCrystal))
					{
						st.takeItems(FilledCrystal, 1);
						st.giveItems(JewelOfValakas, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "watcher_valakas_klein_q10505_10.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 83;
	}
}