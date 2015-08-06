package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.05.12
 * Time: 2:32
 */

public class _00181_DevilsStrikeBackAdventOfBalok extends Quest
{
	// Квестовые персонажи
	private static final int Фиорен = 33044;

	// Квестовые монстры
	private static final int Валлок = 29218;

	// Квестовые предметы
	private static final int КонтрактБелефа = 17592;
	private static final int ЗаточкаБрониR = 17527;
	private static final int ЗаточкаОружияR = 17526;
	private static final int МешочекУсилителей = 34861;

	public _00181_DevilsStrikeBackAdventOfBalok()
	{
		addStartNpc(Фиорен);
		addTalkId(Фиорен);
		addKillId(Валлок);
		questItemIds = new int[]{КонтрактБелефа};
	}

	public static void main(String[] args)
	{
		new _00181_DevilsStrikeBackAdventOfBalok();
	}

	@Override
	public int getQuestId()
	{
		return 181;
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
		else if(event.equalsIgnoreCase("reward"))
		{
			st.addExpAndSp(886750000, 414855000);
			st.giveAdena(37128000, true);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
			int rnd = Rnd.get(2);
			switch(rnd)
			{
				case 0:
					st.giveItems(ЗаточкаОружияR, 2);
					return "33044-09.htm";
				case 1:
					st.giveItems(ЗаточкаБрониR, 2);
					return "33044-10.htm";
				case 2:
					st.giveItems(МешочекУсилителей, 2);
					return "33044-11.htm";
			}
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
			if(npc.getNpcId() == Валлок)
			{
				if(player.getParty() == null)
				{
					st.setCond(2);
					st.giveItem(КонтрактБелефа);
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
							pst.giveItem(КонтрактБелефа);
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
					QuestState prevst = player.getQuestState(_00180_InfernalFlamesBurningInCrystalPrison.class);
					if(player.getLevel() < 97 || prevst == null || !prevst.isCompleted())
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
		QuestState prevst = player.getQuestState(_00180_InfernalFlamesBurningInCrystalPrison.class);
		return player.getLevel() >= 97 && prevst != null && prevst.isCompleted();

	}
}