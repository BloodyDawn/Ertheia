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
 * Date: 14.05.12
 * Time: 2:15
 */

public class _00180_InfernalFlamesBurningInCrystalPrison extends Quest
{
	// Квестовые персонажи
	private static final int Фиорен = 33044;

	// Квестовые монстры
	private static final int Байлор = 29213;

	// Квестовые предметы
	private static final int ЗнакБелефа = 17591;
	private static final int ЗаточкаБрониR = 17527;

	public _00180_InfernalFlamesBurningInCrystalPrison()
	{
		addStartNpc(Фиорен);
		addTalkId(Фиорен);
		addKillId(Байлор);
		questItemIds = new int[]{ЗнакБелефа};
	}

	public static void main(String[] args)
	{
		new _00180_InfernalFlamesBurningInCrystalPrison();
	}

	@Override
	public int getQuestId()
	{
		return 180;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33044-06.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			if(npc.getNpcId() == Байлор)
			{
				if(player.getParty() == null)
				{
					st.setCond(2);
					st.giveItem(ЗнакБелефа);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					QuestState pst;
					for(L2PcInstance pmember : player.getParty().getMembers())
					{
						pst = pmember.getQuestState(getClass());
						if(pst != null && pst.getCond() == 1)
						{
							pst.setCond(2);
							pst.giveItem(ЗнакБелефа);
							pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Фиорен)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 97)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33044-02.htm";
					}
					else
					{
						return "33044-01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33044-07.htm";
					}
					if(st.getCond() == 2)
					{
						st.addExpAndSp(14000000, 6400000);
						st.giveItem(ЗаточкаБрониR);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "33044-08.htm";
					}
					break;
				case COMPLETED:
					return "33044-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;

	}
}