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
 * Date: 09.06.13
 * Time: 9:22
 */
public class _00210_ObtainaWolfPet extends Quest
{
	// Квестовые персонажи
	private static final int LUNDY = 30827;
	private static final int BELLADONA = 30256;
	private static final int BRYNNER = 30335;
	private static final int SYDNEY = 30321;

	public _00210_ObtainaWolfPet()
	{
		addStartNpc(LUNDY);
		addTalkId(LUNDY, BELLADONA, BRYNNER, SYDNEY);
	}

	public static void main(String[] args)
	{
		new _00210_ObtainaWolfPet();
	}

	@Override
	public int getQuestId()
	{
		return 210;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "pet_manager_lundy_q0210_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == LUNDY)
		{
			if(reply == 1)
			{
				return "pet_manager_lundy_q0210_03.htm";
			}
			else if(reply == 10 && st.getCond() == 4)
			{
				st.giveItem(2375);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "pet_manager_lundy_q0210_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == LUNDY)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);// TODO
				case CREATED:
					if(player.getLevel() < 15)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "pet_manager_lundy_q0210_02.htm";
					}
					else
					{
						return "pet_manager_lundy_q0210_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "pet_manager_lundy_q0210_05.htm";
					}
					else if(st.getCond() == 4)
					{
						return "pet_manager_lundy_q0210_06.htm";
					}
			}
		}
		else if(npc.getNpcId() == BELLADONA)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "gatekeeper_belladonna_q0210_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == BRYNNER)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "guard_brynner_q0210_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == SYDNEY)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 3)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "trader_sydney_q0210_01";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 15;
	}
}