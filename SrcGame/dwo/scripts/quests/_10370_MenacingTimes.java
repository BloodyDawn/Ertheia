package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 08.05.12
 * Time: 14:22
 */

public class _10370_MenacingTimes extends Quest
{
	// Квестовые персонажи
	private static final int Орвен = 30857;
	private static final int Винонин = 30856;
	private static final int Олтлин = 30862;
	private static final int Ладанза = 30865;
	private static final int Феррис = 30847;
	private static final int Бром = 32221;
	private static final int Андрей = 31292;
	private static final int Гергхенштейн = 33648;

	// Квестовые монстры
	private static final int[] Монстры = {21647, 21648, 21650};

	// Квестовые предметы
	private static final int ОсколокЗла = 34765;

	public _10370_MenacingTimes()
	{
		addStartNpc(Орвен, Винонин, Олтлин, Ладанза, Феррис, Бром);
		addTalkId(Орвен, Винонин, Олтлин, Ладанза, Феррис, Бром, Андрей, Гергхенштейн);
		addKillId(Монстры);
		questItemIds = new int[]{ОсколокЗла};
	}

	public static void main(String[] args)
	{
		new _10370_MenacingTimes();
	}

	@Override
	public int getQuestId()
	{
		return 10370;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		switch(event)
		{
			case "30857-07.htm":
			case "30856-07.htm":
			case "30862-07.htm":
			case "30865-07.htm":
			case "30847-07.htm":
			case "32221-07.htm":
				st.startQuest();
				break;
			case "31292-03.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "33648-02.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		if(st.getCond() == 3 && Rnd.getChance(50))
		{
			if(ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				st.giveItem(ОсколокЗла);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(ОсколокЗла) >= 30)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Орвен)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getRace() == Race.Human)
						{
							return "30857-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30857-04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30857-03.htm";
					}
				case STARTED:
					return "30857-08.htm";
				case COMPLETED:
					return "30857-05.htm";
			}
		}
		else if(npc.getNpcId() == Винонин)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getRace() == Race.Elf)
						{
							return "30856-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30856-04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30856-03.htm";
					}
				case STARTED:
					return "30856-08.htm";
				case COMPLETED:
					return "30856-05.htm";
			}
		}
		else if(npc.getNpcId() == Олтлин)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getRace() == Race.DarkElf)
						{
							return "30862-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30862-04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30862-03.htm";
					}
				case STARTED:
					return "30862-08.htm";
				case COMPLETED:
					return "30862-05.htm";
			}
		}
		else if(npc.getNpcId() == Ладанза)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getRace() == Race.Orc)
						{
							return "30865-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30865-04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30865-03.htm";
					}
				case STARTED:
					return "30865-08.htm";
				case COMPLETED:
					return "30865-05.htm";
			}
		}
		else if(npc.getNpcId() == Феррис)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getRace() == Race.Orc)
						{
							return "30847-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "30847-04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30847-03.htm";
					}
				case STARTED:
					return "30847-08.htm";
				case COMPLETED:
					return "30847-05.htm";
			}
		}
		else if(npc.getNpcId() == Бром)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getRace() == Race.Kamael)
						{
							return "32221-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "32221-04.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "32221-03.htm";
					}
				case STARTED:
					return "32221-08.htm";
				case COMPLETED:
					return "32221-05.htm";
			}
		}
		else if(npc.getNpcId() == Андрей)
		{
			if(st.getCond() == 1)
			{
				return "31292-01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "31292-04.htm";
			}
		}
		else if(npc.getNpcId() == Гергхенштейн)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 2:
						return "33648-01.htm";
					case 3:
						return "33648-04.htm";
					case 4:
						if(st.getQuestItemsCount(ОсколокЗла) >= 30)
						{
							st.addExpAndSp(22451400, 25202500);
							st.giveAdena(479620, true);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "33648-05.htm";
						}
						break;
				}
			}
			else if(st.isCompleted())
			{
				return "33648-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}
}