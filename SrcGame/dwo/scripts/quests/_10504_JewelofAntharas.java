package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.03.13
 * Time: 14:31
 */

public class _10504_JewelofAntharas extends Quest
{
	// Квестовые персонажи
	private static final int Theodric = 30755;

	// Квестовые монстры
	private static final int[] Antharas = {29019, 29066, 29067, 29068};

	// Квестовые предметы
	private static final int PortalStone = 3865;
	private static final int ClearCrystal = 21905;
	private static final int FilledCrystal = 21907;
	private static final int JewelOfAntharas = 21898;

	public _10504_JewelofAntharas()
	{
		addStartNpc(Theodric);
		addTalkId(Theodric);
		addKillId(Antharas);
	}

	public static void main(String[] args)
	{
		new _10504_JewelofAntharas();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(player.isInsideRadius(npc, 2000, false, false))
		{
			if(st != null && st.getCond() == 1 && ArrayUtils.contains(Antharas, npc.getNpcId()) && st.hasQuestItems(ClearCrystal))
			{
				st.takeItems(ClearCrystal, 1);
				st.giveItems(FilledCrystal, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10504;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(st != null && event.equals("quest_accept") && !st.isCompleted())
		{
			if(!st.isStarted())
			{
				st.startQuest();
				st.giveItems(ClearCrystal, 1);
			}
			return "watcher_antaras_theodric_q10504_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Theodric)
		{
			switch(reply)
			{
				case 1:
					return "watcher_antaras_theodric_q10504_05.htm";
				case 2:
					return "watcher_antaras_theodric_q10504_06.htm";
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

		if(npc.getNpcId() == Theodric)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 84)
					{
						if(st.hasQuestItems(PortalStone))
						{
							return "watcher_antaras_theodric_q10504_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "watcher_antaras_theodric_q10504_04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "watcher_antaras_theodric_q10504_02.htm";
					}
				case COMPLETED:
					return "watcher_antaras_theodric_q10504_03.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						if(st.hasQuestItems(ClearCrystal))
						{
							return "watcher_antaras_theodric_q10504_08.htm";
						}
						else
						{
							st.giveItems(ClearCrystal, 1);
							return "watcher_antaras_theodric_q10504_09.htm";
						}
					}
					if(st.getCond() == 2 && st.hasQuestItems(FilledCrystal))
					{
						st.takeItems(FilledCrystal, 1);
						st.giveItems(JewelOfAntharas, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "watcher_antaras_theodric_q10504_10.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 84;
	}
}