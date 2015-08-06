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
 * Date: 09.08.11
 * Time: 13:05
 */

public class _00117_OceanOfDistantStar extends Quest
{
	// Квестовые персонажи
	private static final int _ABEY = 32053;
	private static final int _GHOST = 32055;
	private static final int _GHOST_F = 32054;
	private static final int _OBI = 32052;
	private static final int _BOX = 32076;
	// Квестовые предметы
	private static final int _GREY_STAR = 8495;
	private static final int _ENGRAVED_HAMMER = 8488;
	// Шансы
	private static final int _CHANCE = 38;

	public _00117_OceanOfDistantStar()
	{
		addStartNpc(_ABEY);
		addTalkId(_ABEY, _GHOST, _GHOST_F, _OBI, _BOX);
		addKillId(22023, 22024);
		questItemIds = new int[]{_GREY_STAR, _ENGRAVED_HAMMER};
	}

	public static void main(String[] args)
	{
		new _00117_OceanOfDistantStar();
	}

	@Override
	public int getQuestId()
	{
		return 117;
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
			case "1":
				event = "0a.htm";
				st.startQuest();
				break;
			case "2":
				event = "1a.htm";
				st.setCond(2);
				break;
			case "3":
				event = "2a.htm";
				st.setCond(3);
				break;
			case "4":
				event = "3a.htm";
				st.setCond(4);
				break;
			case "5":
				event = "4a.htm";
				st.setCond(5);
				st.giveItems(_ENGRAVED_HAMMER, 1);
				break;
			case "6":
				event = "5a.htm";
				st.setCond(6);
				break;
			case "7":
				if(st.hasQuestItems(_ENGRAVED_HAMMER))
				{
					event = "6a.htm";
					st.setCond(7);
				}
				break;
			case "8":
				if(st.hasQuestItems(_GREY_STAR))
				{
					event = "7a.htm";
					st.takeItems(_GREY_STAR, -1);
					st.setCond(9);
				}
				break;
			case "9":
				if(st.hasQuestItems(_ENGRAVED_HAMMER))
				{
					event = "8a.htm";
					st.takeItems(_ENGRAVED_HAMMER, -1);
					st.setCond(10);
				}
				break;
			case "10":
				event = "9b.htm";
				st.giveAdena(17647, true);
				st.addExpAndSp(107387, 7369);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || !st.isStarted())
		{
			return null;
		}

		if(st.getCond() == 7 && !st.hasQuestItems(_GREY_STAR) && Rnd.getChance(_CHANCE))
		{
			st.giveItems(_GREY_STAR, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			st.setCond(8);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(st.getState() == COMPLETED)
		{
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		if(st.getState() == CREATED)
		{
			if(npcId == _ABEY)
			{
				if(player.getLevel() >= 39)
				{
					return "0.htm";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "Этот квест может быть взят только игроками, достигшими 39 уровня.";
				}
			}
		}
		else if(st.getState() == STARTED)
		{
			switch(npcId)
			{
				case _GHOST:
					if(cond == 1)
					{
						return "1.htm";
					}
					if(cond == 9 && st.hasQuestItems(_ENGRAVED_HAMMER))
					{
						return "8.htm";
					}
					break;
				case _OBI:
					if(cond == 2)
					{
						return "2.htm";
					}
					if(cond == 6 && st.hasQuestItems(_ENGRAVED_HAMMER))
					{
						return "6.htm";
					}
					if(cond == 7 && st.hasQuestItems(_ENGRAVED_HAMMER))
					{
						return "6a.htm";
					}
					if(cond == 8 && st.hasQuestItems(_GREY_STAR))
					{
						return "7.htm";
					}
					break;
				case _ABEY:
					if(cond == 3)
					{
						return "3.htm";
					}
					if(cond == 5 && st.hasQuestItems(_ENGRAVED_HAMMER))
					{
						return "5.htm";
					}
					if(cond == 6 && st.hasQuestItems(_ENGRAVED_HAMMER))
					{
						return "5a.htm";
					}
					break;
				case _BOX:
					if(cond == 4)
					{
						return "4.htm";
					}
					break;
				case _GHOST_F:
					if(cond == 10)
					{
						return "9.htm";
					}
					break;
			}
		}
		return null;
	}
}