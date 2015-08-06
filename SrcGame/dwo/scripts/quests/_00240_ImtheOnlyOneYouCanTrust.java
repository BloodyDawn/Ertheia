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
 * Date: 17.08.12
 * Time: 13:57
 */

public class _00240_ImtheOnlyOneYouCanTrust extends Quest
{
	// Квестовые персонажи
	private static final int KINTAIJIN = 32640;

	// Квестовые мобы
	private static final int[] MOBS = {22617, 22618, 22619, 22621, 22622, 22627, 22628, 22629, 22624};

	// Квестовые предметы
	private static final int STAKATOFANGS = 14879;

	public _00240_ImtheOnlyOneYouCanTrust()
	{
		addStartNpc(KINTAIJIN);
		addTalkId(KINTAIJIN);
		addKillId(MOBS);
		questItemIds = new int[]{STAKATOFANGS};
	}

	public static void main(String[] args)
	{
		new _00240_ImtheOnlyOneYouCanTrust();
	}

	@Override
	public int getQuestId()
	{
		return 240;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "kintaijin_q0240_09.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		if(npcId == KINTAIJIN)
		{
			switch(reply)
			{
				case 1:
					return "kintaijin_q0240_04.htm";
				case 2:
					return "kintaijin_q0240_05.htm";
				case 3:
					return "kintaijin_q0240_06.htm";
				case 4:
					return "kintaijin_q0240_07.htm";
				case 5:
					return "kintaijin_q0240_08.htm";

			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || isPet)
		{
			return null;
		}

		if(!st.isStarted())
		{
			return null;
		}

		st.giveItems(STAKATOFANGS, 1);
		if(st.getQuestItemsCount(STAKATOFANGS) >= 25)
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == KINTAIJIN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() >= 81)
					{
						return "kintaijin_q0240_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "kintaijin_q0240_02.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "kintaijin_q0240_10.htm";
					}
					if(cond == 2)
					{
						if(st.getQuestItemsCount(STAKATOFANGS) < 25)
						{
							return "kintaijin_q0240_11.htm";
						}
						else
						{
							st.takeItems(STAKATOFANGS, -1);
							st.giveAdena(1351512, true);
							st.addExpAndSp(6411717, 7456914);
							st.exitQuest(QuestType.ONE_TIME);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							return "kintaijin_q0240_12.htm";
						}
					}
					break;
				case COMPLETED:
					return "kintaijin_q0240_03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 81;
	}
}
