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
 * Date: 25.12.12
 * Time: 2:47
 */

public class _00359_ForSleeplessDeadmen extends Quest
{
	private static final int DROP_RATE = 10;
	private static final int REQUIRED = 60;

	// Квестовые предметы
	private static final int REMAINS = 5869;

	// Награды
	private static final int PhoenixEarrPart = 6341;
	private static final int MajEarrPart = 6342;
	private static final int PhoenixNeclPart = 6343;
	private static final int MajNeclPart = 6344;
	private static final int PhoenixRingPart = 6345;
	private static final int MajRingPart = 6346;

	private static final int DarkCryShieldPart = 5494;
	private static final int NightmareShieldPart = 5495;

	// Квестовые персонажи
	private static final int ORVEN = 30857;

	// Квестовые монстры
	private static final int DOOMSERVANT = 21006;
	private static final int DOOMGUARD = 21007;
	private static final int DOOMARCHER = 21008;
	private static final int DOOMTROOPER = 21009;

	public _00359_ForSleeplessDeadmen()
	{
		addStartNpc(ORVEN);
		addTalkId(ORVEN);
		addKillId(DOOMSERVANT, DOOMGUARD, DOOMARCHER, DOOMTROOPER);
		questItemIds = new int[]{REMAINS};
	}

	public static void main(String[] args)
	{
		new _00359_ForSleeplessDeadmen();
	}

	@Override
	public int getQuestId()
	{
		return 359;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			qs.setMemoState(1);
			return "highpriest_orven_q0359_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(reply == 1)
		{
			return "highpriest_orven_q0359_05.htm";
		}
		if(reply == 2 && st.getMemoState() == 2)
		{
			st.exitQuest(QuestType.REPEATABLE);
			int i0 = Rnd.get(8);
			switch(i0)
			{
				case 0:
					st.giveItems(PhoenixEarrPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 1:
					st.giveItems(PhoenixNeclPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 2:
					st.giveItems(PhoenixRingPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 3:
					st.giveItems(MajEarrPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 4:
					st.giveItems(MajNeclPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 5:
					st.giveItems(MajRingPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 6:
					st.giveItems(DarkCryShieldPart, 4);
					return "highpriest_orven_q0359_10.htm";
				case 7:
					st.giveItems(NightmareShieldPart, 4);
					return "highpriest_orven_q0359_10.htm";
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		long count = st.getQuestItemsCount(REMAINS);
		if(count < REQUIRED && Rnd.getChance(DROP_RATE))
		{
			st.giveItems(REMAINS, 1);
			if(count + 1 >= REQUIRED)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return htmltext;
		}

		if(npc.getNpcId() == ORVEN)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() < 60 ? "highpriest_orven_q0359_01.htm" : "highpriest_orven_q0359_02.htm";
				case STARTED:
					if(st.getMemoState() == 1)
					{
						if(player.getItemsCount(REMAINS) < 60)
						{
							return "highpriest_orven_q0359_07.htm";
						}
						else
						{
							st.takeItems(REMAINS, -1);
							st.setMemoState(2);
							st.setCond(3);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "highpriest_orven_q0359_08.htm";
						}
					}
					else if(st.getMemoState() == 2)
					{
						return "highpriest_orven_q0359_09.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 60;

	}
}