package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 18.04.12
 * Time: 3:05
 */

public class _10316_UndecayingMemoryOfThePast extends Quest
{
	// Квестовые персонажи
	private static final int Опера = 32946;

	// Квестовые монстры
	private static final int Спасия = 25779;

	// Квестовые награды
	private static final int РукоятьКузнецаГигантов = 19305;
	private static final int ЗаготовкаРеоринаГигантов = 19306;
	private static final int НаковальняКузнецаГигантов = 19307;
	private static final int ЗаготовкаОружейникаГигантов = 19308;
	private static final int СвитокR = 17527;
	private static final int МешочекR = 34861;

	public _10316_UndecayingMemoryOfThePast()
	{
		addStartNpc(Опера);
		addTalkId(Опера);
		addKillId(Спасия);
	}

	public static void main(String[] args)
	{
		new _10316_UndecayingMemoryOfThePast();
	}

	@Override
	public int getQuestId()
	{
		return 10316;
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
			case "32946-06.htm":
				st.startQuest();
				break;
			case "32946-12.htm":
				st.addExpAndSp(54093924, 23947602);
				st.giveItem(РукоятьКузнецаГигантов);
				st.giveItem(ЗаготовкаРеоринаГигантов);
				st.giveItem(НаковальняКузнецаГигантов);
				st.giveItem(ЗаготовкаОружейникаГигантов);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "32946-13.htm":
				st.addExpAndSp(54093924, 23947602);
				st.giveItems(СвитокR, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "32946-14.htm":
				st.addExpAndSp(54093924, 23947602);
				st.giveItems(МешочекR, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
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

		if(st.getCond() == 1 && npc.getNpcId() == Спасия)
		{
			if(killer.getParty() == null)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			else
			{
				for(L2PcInstance partyMember : killer.getParty().getMembers())
				{
					if(Util.checkIfInRange(900, killer, partyMember, false))
					{
						st = partyMember.getQuestState(getClass());
						if(st != null && st.isStarted())
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(2);
						}
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState previous = player.getQuestState(_10315_ToThePrisonOfDarkness.class);

		if(npc.getNpcId() == Опера)
		{
			if(previous == null || !previous.isCompleted())
			{
				if(player.getLevel() < 90)
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32946-02.htm";
				}
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32946-09.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "32946-10.htm";
				case CREATED:
					return "32946-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32946-07.htm";
					}
					if(st.getCond() == 2)
					{
						return "32946-08.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10315_ToThePrisonOfDarkness.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}
}
