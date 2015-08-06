package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 08.05.12
 * Time: 23:08
 */

public class _10372_PurgatoryVolvere extends Quest
{
	// Квестовые персонажи
	private static final int Gerhenstein = 33648;
	private static final int Andrew = 31292;

	// Квестовые монстры
	private static final int BloodyNightmare = 23185;

	// Квестовые предметы
	private static final int BloodyEssence = 34766;
	private static final int GerhensteinReport = 34767;

	public _10372_PurgatoryVolvere()
	{
		addStartNpc(Gerhenstein);
		addTalkId(Gerhenstein, Andrew);
		addKillId(BloodyNightmare);
		questItemIds = new int[]{BloodyEssence, GerhensteinReport};
	}

	public static void main(String[] args)
	{
		new _10372_PurgatoryVolvere();
	}

	@Override
	public int getQuestId()
	{
		return 10372;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "gerkenstein_q10372_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Gerhenstein)
		{
			if(reply == 1)
			{
				return "gerkenstein_q10372_04.htm";
			}
			else if(reply == 2)
			{
				return "gerkenstein_q10372_05.htm";
			}
		}
		else if(npc.getNpcId() == Andrew)
		{
			if(st.getCond() == 3)
			{
				int crystallId = 0;
				switch(reply)
				{
					case 1:
						return "captain_andrei_q10372_02.htm";
					case 2:
						return "captain_andrei_q10372_03.htm";
					case 11:
						crystallId = 9552;
						break;
					case 12:
						crystallId = 9553;
						break;
					case 13:
						crystallId = 9554;
						break;
					case 14:
						crystallId = 9555;
						break;
					case 15:
						crystallId = 9556;
						break;
					case 16:
						crystallId = 9557;
						break;
				}

				if(reply > 10 && crystallId > 0)
				{
					st.giveItem(crystallId);
					st.addExpAndSp(23009000, 2644010);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "captain_andrei_q10372_04.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		if(st.getCond() == 1)
		{
			if(npc.getNpcId() == BloodyNightmare)
			{
				st.giveItem(BloodyEssence);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(BloodyEssence) >= 10)
				{
					st.setCond(2);
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

		if(npc.getNpcId() == Gerhenstein)
		{
			switch(st.getState())
			{
				case CREATED:

					if(canBeStarted(player))
					{
						return "gerkenstein_q10372_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "gerkenstein_q10372_02.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "gerkenstein_q10372_07.htm";
						case 2:
							st.setCond(3);
							st.takeItems(BloodyEssence, -1);
							st.giveItem(GerhensteinReport);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "gerkenstein_q10372_08.htm";
						case 3:
							return "gerkenstein_q10372_09.htm";
					}
					break;
				case COMPLETED:
					return "gerkenstein_q10372_03.htm";
			}
		}
		else if(npc.getNpcId() == Andrew)
		{
			if(st.isStarted() && st.getCond() == 3)
			{
				return "captain_andrei_q10372_01.htm";
			}
			else if(st.isCompleted())
			{
				return "captain_andrei_q10372_05.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(_10371_GraspThyPower.class);
		return st != null && st.isCompleted() && player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}
}
