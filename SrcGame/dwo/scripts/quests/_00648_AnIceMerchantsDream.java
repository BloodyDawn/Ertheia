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
 * Time: 3:23
 */

public class _00648_AnIceMerchantsDream extends Quest
{
	// Квестовые персонажи
	private static int Rafforty = 32020;
	private static int Ice_Shelf = 32023;

	// Квестовые предметы
	private static int Silver_Hemocyte = 8057;
	private static int Silver_Ice_Crystal = 8077;
	private static int Black_Ice_Crystal = 8078;

	// Шансы
	private static int Silver_Hemocyte_Chance = 2;

	public _00648_AnIceMerchantsDream()
	{
		addStartNpc(Rafforty);
		addStartNpc(Ice_Shelf);
		addTalkId(Rafforty);
		addTalkId(Ice_Shelf);
		for(int i = 22080; i <= 22098; i++)
		{
			if(i != 22095)
			{
				addKillId(i);
			}
		}
	}

	public static void main(String[] args)
	{
		new _00648_AnIceMerchantsDream();
	}

	@Override
	public int getQuestId()
	{
		return 648;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			if(qs.getPlayer().getLevel() >= 53)
			{
				qs.startQuest();
				QuestState prevSt = qs.getPlayer().getQuestState(_00115_TheOtherSideOfTruth.class);
				if(prevSt == null || !prevSt.isCompleted())
				{
					qs.setMemoState(1);
					return "repre_q0648_04.htm";
				}
				else
				{
					qs.setCond(2);
					qs.setMemoState(2);
					return "repre_q0648_05.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Rafforty)
		{
			QuestState prevSt = player.getQuestState(_00115_TheOtherSideOfTruth.class);
			if(reply == 1 && st.getMemoState() >= 1)
			{
				return prevSt == null || !prevSt.isCompleted() ? "repre_q0648_12.htm" : "repre_q0648_13.htm";
			}
			else if(reply == 2 && st.getMemoState() >= 1)
			{
				return prevSt != null && prevSt.isCompleted() ? "repre_q0648_21.htm" : "repre_q0648_20.htm";
			}
			else if(reply == 3 && st.getMemoState() >= 1)
			{
				if(prevSt == null || !prevSt.isCompleted())
				{
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "repre_q0648_22.htm";
				}
				else
				{
					return "repre_q0648_23.htm";
				}
			}
			else if(reply == 4 && st.getMemoState() >= 1)
			{
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "repre_q0648_24.htm";
			}
			else if(reply == 10 && st.getMemoState() >= 1)
			{
				if(player.getItemsCount(Silver_Ice_Crystal) + player.getItemsCount(Black_Ice_Crystal) > 0)
				{
					st.giveAdena(300 * player.getItemsCount(Silver_Ice_Crystal) + 1200 * player.getItemsCount(Black_Ice_Crystal), true);
					st.takeItems(Silver_Ice_Crystal, -1);
					st.takeItems(Black_Ice_Crystal, -1);
					return "repre_q0648_14.htm";
				}
				else
				{
					return "repre_q0648_16a.htm";
				}
			}
			else if(reply == 11 && st.getMemoState() >= 1)
			{
				if(player.getItemsCount(Silver_Ice_Crystal) + player.getItemsCount(Black_Ice_Crystal) > 0)
				{
					st.giveAdena(300 * player.getItemsCount(Silver_Ice_Crystal) + 1200 * player.getItemsCount(Black_Ice_Crystal), true);
					st.takeItems(Silver_Ice_Crystal, -1);
					st.takeItems(Black_Ice_Crystal, -1);
					return "repre_q0648_15.htm";
				}
				else
				{
					return "repre_q0648_16a.htm";
				}
			}
		}
		else if(npc.getNpcId() == Ice_Shelf)
		{
			if(reply == 101 && st.getMemoState() >= 1 && player.getItemsCount(Silver_Ice_Crystal) > 0)
			{
				if(st.getMemoStateEx(1) == 0)
				{
					int i0 = Rnd.get(4) + 1;
					int i1 = i0 * 10;
					st.setMemoStateEx(1, i1);
					return "ice_lathe_q0648_05.htm";
				}
			}
			else if(reply == 102 && st.getMemoState() >= 1 && st.getMemoStateEx(1) > 0 && player.getItemsCount(Silver_Ice_Crystal) > 0)
			{
				st.takeItems(Silver_Ice_Crystal, 1);
				int i0 = st.getMemoStateEx(1);
				int i1 = i0 + 1;
				st.setMemoStateEx(1, i1);
				st.playSound(QuestSound.ITEMSOUND_BROKEN_KEY);
				return "ice_lathe_q0648_06.htm";
			}
			else if(reply == 103 && st.getMemoState() >= 1 && st.getMemoStateEx(1) > 0 && player.getItemsCount(Silver_Ice_Crystal) > 0)
			{
				st.takeItems(Silver_Ice_Crystal, 1);
				int i0 = st.getMemoStateEx(1);
				int i1 = i0 + 2;
				st.setMemoStateEx(1, i1);
				st.playSound(QuestSound.ITEMSOUND_BROKEN_KEY);
				return "ice_lathe_q0648_07.htm";
			}
			else if(reply == 4 && st.getMemoState() >= 1 && st.getMemoStateEx(1) > 0)
			{
				int i0 = st.getMemoStateEx(1);
				int i1 = i0 / 10;
				int i2 = i0 - i1 * 10;
				st.setMemoStateEx(1, 0);
				if(i1 == i2)
				{
					st.giveItem(Black_Ice_Crystal);
					st.playSound(QuestSound.ITEMSOUND_ENCHANT_SUCCESS);
					return "ice_lathe_q0648_08.htm";
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_ENCHANT_FAILED);
					return "ice_lathe_q0648_09.htm";
				}

			}
			else if(reply == 5 && st.getMemoState() >= 1 && st.getMemoStateEx(1) > 0)
			{
				int i0 = st.getMemoStateEx(1);
				int i1 = i0 / 10;
				int i2 = i0 - i1 * 10;
				st.setMemoStateEx(1, 0);
				if(i1 == i2 + 2)
				{
					st.giveItem(Silver_Ice_Crystal);
					st.playSound(QuestSound.ITEMSOUND_ENCHANT_SUCCESS);
					return "ice_lathe_q0648_10.htm";
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_ENCHANT_FAILED);
					return "ice_lathe_q0648_11.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(player != null)
		{
			if(Rnd.getChance(npc.getNpcId() - 22050))
			{
				st.giveItems(Silver_Ice_Crystal, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}

		if(st.isStarted() && st.getCond() == 2 && Rnd.getChance(Silver_Hemocyte_Chance))
		{
			st.giveItems(Silver_Hemocyte, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		QuestState prevSt = player.getQuestState(_00115_TheOtherSideOfTruth.class);
		if(st == null)
		{
			return htmltext;
		}
		if(npc.getNpcId() == Rafforty)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 53)
					{
						return "repre_q0648_01.htm";
					}
					else
					{
						return prevSt != null && prevSt.isCompleted() ? "repre_q0648_02.htm" : "repre_q0648_03.htm";
					}
				case STARTED:
					if(player.getItemsCount(Silver_Ice_Crystal) + player.getItemsCount(Black_Ice_Crystal) == 0)
					{
						if(prevSt == null || !prevSt.isCompleted())
						{
							return "repre_q0648_08.htm";
						}
						else
						{
							st.setCond(2);
							st.setMemoState(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "repre_q0648_09.htm";
						}
					}
					else
					{
						if(prevSt == null || !prevSt.isCompleted())
						{
							return "repre_q0648_10.htm";
						}
						else
						{
							st.setCond(2);
							st.setMemoState(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "repre_q0648_11.htm";
						}
					}
			}
		}
		else if(npc.getNpcId() == Ice_Shelf)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() >= 1 && player.getItemsCount(Silver_Ice_Crystal) == 0)
				{
					return "ice_lathe_q0648_02.htm";
				}
				else if(st.getMemoStateEx(1) % 10 == 0 && player.getItemsCount(Silver_Ice_Crystal) > 0)
				{
					return "ice_lathe_q0648_03.htm";
				}
				else if(st.getMemoStateEx(1) % 10 > 0)
				{
					return "ice_lathe_q0648_04.htm";
				}
			}
			else
			{
				return "ice_lathe_q0648_01.htm";
			}
		}
		return null;
	}
}