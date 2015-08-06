package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 22.04.12
 * Time: 2:38
 */

public class _10309_MysteriousLetter extends Quest
{
	// Квестовые персонажи
	private static final int ГидПутешественников = 33463;
	private static final int Алиша = 31303;
	private static final int Тифарен = 31334;
	private static final int ЗагадочныйМаг = 31522;

	public _10309_MysteriousLetter()
	{
		setMinMaxLevel(65, 69);
		addStartNpc(ГидПутешественников);
		addTalkId(ГидПутешественников, Алиша, Тифарен, ЗагадочныйМаг);
	}

	public static void main(String[] args)
	{
		new _10309_MysteriousLetter();
	}

	@Override
	public int getQuestId()
	{
		return 10309;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("33463-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("31334-02.htm"))
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31522-02.htm"))
		{
			st.addExpAndSp(4952910, 4894920);
			st.giveAdena(276000, true);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == ГидПутешественников)
		{
			switch(st.getState())
			{
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33463-04.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			}
		}
		else if(npc.getNpcId() == Алиша)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "31303-01.htm";
				}
				else if(st.getCond() == 2)
				{
					return "31303-02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31303-03.htm";
			}
		}
		else if(npc.getNpcId() == Тифарен)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "31334-01.htm";
				}
				else if(st.getCond() == 3)
				{
					return "31334-03.htm";
				}
			}
		}
		else if(npc.getNpcId() == ЗагадочныйМаг)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 3)
				{
					return "31522-01.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31522-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 65 && player.getLevel() <= 69;

	}
}