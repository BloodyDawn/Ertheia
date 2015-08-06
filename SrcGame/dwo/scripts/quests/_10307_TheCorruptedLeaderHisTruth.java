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
 * @author ANZO
 * Date: 12.04.12
 * Time: 7:47
 */

public class _10307_TheCorruptedLeaderHisTruth extends Quest
{
	// Квестовые персонажи
	private static final int Кхишару = 32896;
	private static final int Мимирид = 32895;

	// Квестовые монстры
	private static final int[] Кимериан = {25745, 25747};

	// Квестовые предметы
	private static final int СвитокR = 17527;

	public _10307_TheCorruptedLeaderHisTruth()
	{
		addStartNpc(Кхишару);
		addTalkId(Кхишару, Мимирид);
		addKillId(Кимериан);
	}

	public static void main(String[] args)
	{
		new _10307_TheCorruptedLeaderHisTruth();
	}

	@Override
	public int getQuestId()
	{
		return 10307;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "32896-05.htm":
				st.startQuest();
				break;
			case "32896-08.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32895-04.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(11779522, 5275253);
				st.giveItem(СвитокR);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}
		if(ArrayUtils.contains(Кимериан, npc.getNpcId()))
		{
			if(st.getCond() == 1)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prevst = player.getQuestState(_10306_FallenLeader.class);

		if(npc.getNpcId() == Кхишару)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "32896-02.htm";
				case CREATED:
					if(player.getLevel() >= 90)
					{
						if(prevst != null && prevst.isCompleted())
						{
							return "32896-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "32896-03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "32896-03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32896-05.htm";
					}
					if(st.getCond() == 2)
					{
						return "32896-06.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Мимирид)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 3)
				{
					return "32895-01.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "32895-05.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10306_FallenLeader.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}
}