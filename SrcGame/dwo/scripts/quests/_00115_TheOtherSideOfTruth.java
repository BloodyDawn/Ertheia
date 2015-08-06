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
 * Date: 06.11.12
 * Time: 20:40
 */

public class _00115_TheOtherSideOfTruth extends Quest
{
	// Квестовые персонажи
	private static final int Misa = 32018;
	private static final int Rafforty = 32020;
	private static final int Sculpture1 = 32021;
	private static final int Kierre = 32022;
	private static final int Sculpture2 = 32077;
	private static final int Sculpture3 = 32078;
	private static final int Sculpture4 = 32079;

	// Квестовые предметы
	private static final int Letter = 8079;
	private static final int Letter2 = 8080;
	private static final int Tablet = 8081;
	private static final int Report = 8082;

	public _00115_TheOtherSideOfTruth()
	{
		addStartNpc(Rafforty);
		addTalkId(Rafforty, Misa, Sculpture1, Sculpture2, Sculpture3, Sculpture4, Kierre);
		questItemIds = new int[]{Letter, Letter2, Tablet, Report};
	}

	public static void main(String[] args)
	{
		new _00115_TheOtherSideOfTruth();
	}

	@Override
	public int getQuestId()
	{
		return 115;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(1);
			return "repre_q0115_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == Rafforty)
		{
			switch(reply)
			{
				case 1:
					if(st.getMemoState() == 2)
					{
						st.setCond(3);
						st.setMemoState(3);
						st.takeItems(Letter, -1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "repre_q0115_07.htm";
					}
					break;
				case 2:
					if(st.getMemoState() == 2)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.REPEATABLE);
						return "repre_q0115_08.htm";
					}
					break;
				case 3:
					if(st.getMemoState() == 3)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.setCond(4);
						st.setMemoState(4);
						return "repre_q0115_11.htm";
					}
					break;
				case 4:
					if(st.getMemoState() == 3)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.setCond(4);
						st.setMemoState(4);
						return "repre_q0115_12.htm";
					}
					break;
				case 5:
					if(st.getMemoState() == 3)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.REPEATABLE);
						return "repre_q0115_13.htm";
					}
					break;
				case 6:
					if(st.getMemoState() == 4)
					{
						st.playSound(QuestSound.AMBIENT_SOUND_WINGFLAP);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.setCond(5);
						st.setMemoState(5);
						return "repre_q0115_17.htm";
					}
					break;
				case 8:
					if(st.getMemoState() == 9)
					{
						if(st.hasQuestItems(Report))
						{
							st.takeItems(Report, -1);
							st.setMemoState(10);
							st.setCond(10);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "repre_q0115_23.htm";
						}
					}
					break;
				case 9:
					if(st.getMemoState() == 10)
					{
						if(st.hasQuestItems(Tablet))
						{
							st.giveAdena(115673, true);
							st.addExpAndSp(493595, 40442);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "repre_q0115_25.htm";
						}
						else
						{
							st.setMemoState(11);
							st.setCond(11);
							st.playSound(QuestSound.AMBIENT_SOUND_THUNDER);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "repre_q0115_27.htm";
						}
					}
					break;
				case 10:
					if(st.getMemoState() == 10)
					{
						if(st.hasQuestItems(Tablet))
						{
							st.giveAdena(115673, true);
							st.addExpAndSp(493595, 40442);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "repre_q0115_26.htm";
						}
						else
						{
							st.setMemoState(11);
							st.setCond(11);
							st.playSound(QuestSound.AMBIENT_SOUND_THUNDER);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "repre_q0115_28.htm";
						}
					}
			}
		}
		else if(npcId == Kierre)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 8)
				{
					st.giveItem(Report);
					st.setMemoState(9);
					st.setCond(9);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "keier_q0115_02.htm";
				}
			}
		}
		else if(npcId == Misa)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 6)
				{
					st.takeItems(Letter2, -1);
					st.setMemoState(7);
					st.setCond(7);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "misa_q0115_05.htm";
				}
			}
		}
		else if(npcId == Sculpture1)
		{
			if(st.getMemoStateEx(1) % 2 < 1)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 6 || i0 == 10 || i0 == 12)
				{
					st.setMemoStateEx(1, i0 + 1);
					st.giveItem(Tablet);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "ice_sculpture_q0115_03.htm";
				}
			}
			if(st.getMemoStateEx(1) % 2 < 1)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 6 || i0 == 10 || i0 == 12)
				{
					st.setMemoStateEx(1, i0 + 1);
					return "ice_sculpture_q0115_04.htm";
				}
			}
			if(reply == 3)
			{
				if(st.getMemoState() == 7 && st.getMemoStateEx(1) == 14)
				{
					st.setMemoState(8);
					st.setCond(8);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "ice_sculpture_q0115_06.htm";
				}
			}
		}
		else if(npcId == Sculpture2)
		{
			if(st.getMemoStateEx(1) % 4 < 1)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 5 || i0 == 9 || i0 == 12)
				{
					st.setMemoStateEx(1, i0 + 2);
					st.giveItem(Tablet);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "ice_sculpture_q0115_03.htm";
				}
			}
			if(st.getMemoStateEx(1) % 4 < 1)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 5 || i0 == 9 || i0 == 12)
				{
					st.setMemoStateEx(1, i0 + 2);
					return "ice_sculpture_q0115_04.htm";
				}
			}
			if(reply == 3)
			{
				if(st.getMemoState() == 7 && st.getMemoStateEx(1) == 13)
				{
					st.setMemoState(8);
					st.setCond(8);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "ice_sculpture_q0115_06.htm";
				}
			}
		}
		else if(npcId == Sculpture3)
		{
			if(st.getMemoStateEx(1) % 8 < 3)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 3 || i0 == 9 || i0 == 10)
				{
					st.setMemoStateEx(1, i0 + 4);
					st.giveItem(Tablet);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "ice_sculpture_q0115_03.htm";
				}
			}
			if(st.getMemoStateEx(1) % 8 < 3)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 3 || i0 == 9 || i0 == 10)
				{
					st.setMemoStateEx(1, i0 + 4);
					return "ice_sculpture_q0115_04.htm";
				}
			}
			if(reply == 3)
			{
				if(st.getMemoState() == 7 && st.getMemoStateEx(1) == 11)
				{
					st.setMemoState(8);
					st.setCond(8);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "ice_sculpture_q0115_06.htm";
				}
			}
		}
		else if(npcId == Sculpture4)
		{
			if(reply == 1 && st.getMemoState() == 7 && st.getMemoStateEx(1) <= 7)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 3 || i0 == 5 || i0 == 6)
				{
					st.setMemoStateEx(1, i0 + 8);
					st.giveItem(Tablet);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					return "ice_sculpture_q0115_03.htm";
				}
			}
			else if(reply == 2 && st.getMemoState() == 7 && st.getMemoStateEx(1) <= 7)
			{
				int i0 = st.getMemoStateEx(1);
				if(i0 == 3 || i0 == 5 || i0 == 6)
				{
					st.setMemoStateEx(1, i0 + 8);
					return "ice_sculpture_q0115_04.htm";
				}
			}
			else if(reply == 3 && st.getMemoState() == 7 && st.getMemoStateEx(1) == 7)
			{
				st.setMemoState(8);
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "ice_sculpture_q0115_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		L2PcInstance player = st.getPlayer();

		if(npcId == Rafforty)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 53 ? "repre_q0115_01.htm" : "repre_q0115_02.htm";
				case STARTED:
					switch(st.getMemoState())
					{
						case 1:
							return "repre_q0115_04.htm";
						case 2:
							if(st.hasQuestItems(Letter))
							{
								return "repre_q0115_06.htm";
							}
							else
							{
								st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
								st.exitQuest(QuestType.REPEATABLE);
								return "repre_q0115_05.htm";
							}
						case 3:
							return "repre_q0115_09.htm";
						case 4:
							return "repre_q0115_16.htm";
						case 5:
							st.giveItem(Letter2);
							st.setMemoState(6);
							st.setCond(6);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "repre_q0115_18.htm";
						case 6:
							if(st.hasQuestItems(Letter2))
							{
								return "repre_q0115_19.htm";
							}
							else
							{
								st.giveItem(Letter2);
								return "repre_q0115_20.htm";
							}
						case 7:
						case 8:
							return "repre_q0115_21.htm";
						case 9:
							if(st.hasQuestItems(Report))
							{
								return "repre_q0115_22.htm";
							}
							break;
						case 10:
							return "repre_q0115_24.htm";
						case 11:
							if(st.hasQuestItems(Tablet))
							{
								st.exitQuest(QuestType.ONE_TIME);
								st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
								st.giveAdena(115673, true);
								st.addExpAndSp(493595, 40442);
								return "repre_q0115_30.htm";
							}
							else
							{
								return "repre_q0115_29.htm";
							}
					}
				case COMPLETED:
					return "finishedquest.htm";
			}
		}
		else if(npcId == Misa)
		{
			if(st.isStarted())
			{
				switch(st.getMemoState())
				{
					case 1:
						st.giveItem(Letter);
						st.setMemoState(2);
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "misa_q0115_02.htm";
					case 2:
						return "misa_q0115_03.htm";
					case 3:
					case 4:
						return "misa_q0115_01.htm";
					case 6:
						if(st.hasQuestItems(Letter2))
						{
							return "misa_q0115_04.htm";
						}
						break;
					case 7:
						return "misa_q0115_06.htm";
				}
			}
		}
		else if(npcId == Kierre)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 8)
				{
					return "keier_q0115_01.htm";
				}
				else if(st.getMemoState() == 9)
				{
					if(st.hasQuestItems(Report))
					{
						return "keier_q0115_03.htm";
					}
					else
					{
						st.giveItem(Report);
						return "keier_q0115_04.htm";
					}
				}
				else if(st.getMemoState() == 11)
				{
					if(!st.hasQuestItems(Report))
					{
						return "keier_q0115_05.htm";
					}
				}
			}
		}
		else if(npcId == Sculpture1)
		{
			if(st.isStarted())
			{
				switch(st.getMemoState())
				{
					case 7:
						if(st.getMemoStateEx(1) % 2 < 1)
						{
							int memoStateEx = st.getMemoStateEx(1);
							if(memoStateEx == 6 || memoStateEx == 10 || memoStateEx == 12)
							{
								return "ice_sculpture_q0115_02.htm";
							}
							else if(memoStateEx == 14)
							{
								return "ice_sculpture_q0115_05.htm";
							}
							else
							{
								st.setMemoStateEx(1, memoStateEx + 1);
								return "ice_sculpture_q0115_01.htm";
							}
						}
						else
						{
							return "ice_sculpture_q0115_01a.htm";
						}
					case 8:
						return "ice_sculpture_q0115_07.htm";
					case 11:
						if(st.hasQuestItems(Tablet))
						{
							return "ice_sculpture_q0115_09.htm";
						}
						else
						{
							st.giveItem(Tablet);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(12);
							return "ice_sculpture_q0115_08.htm";
						}
				}
			}
		}
		else if(npcId == Sculpture2)
		{
			if(st.isStarted())
			{
				switch(st.getMemoState())
				{
					case 7:
						if(st.getMemoStateEx(1) % 4 <= 1)
						{
							int memoStateEx = st.getMemoStateEx(1);
							if(memoStateEx == 5 || memoStateEx == 9 || memoStateEx == 12)
							{
								return "ice_sculpture_q0115_02.htm";
							}
							else if(memoStateEx == 13)
							{
								return "ice_sculpture_q0115_05.htm";
							}
							else
							{
								st.setMemoStateEx(1, memoStateEx + 2);
								return "ice_sculpture_q0115_01.htm";
							}
						}
						else
						{
							return "ice_sculpture_q0115_01a.htm";
						}
					case 8:
						return "ice_sculpture_q0115_07.htm";
					case 11:
						if(st.hasQuestItems(Tablet))
						{
							return "ice_sculpture_q0115_09.htm";
						}
						else
						{
							st.giveItem(Tablet);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(12);
							return "ice_sculpture_q0115_08.htm";
						}
				}
			}
		}
		else if(npcId == Sculpture3)
		{
			if(st.isStarted())
			{
				switch(st.getMemoState())
				{
					case 7:
						if(st.getMemoStateEx(1) % 8 <= 3)
						{
							int memoStateEx = st.getMemoStateEx(1);
							if(memoStateEx == 3 || memoStateEx == 9 || memoStateEx == 10)
							{
								return "ice_sculpture_q0115_02.htm";
							}
							else if(memoStateEx == 11)
							{
								return "ice_sculpture_q0115_05.htm";
							}
							else
							{
								st.setMemoStateEx(1, memoStateEx + 4);
								return "ice_sculpture_q0115_01.htm";
							}
						}
						else
						{
							return "ice_sculpture_q0115_01a.htm";
						}
					case 8:
						return "ice_sculpture_q0115_07.htm";
					case 11:
						if(st.hasQuestItems(Tablet))
						{
							return "ice_sculpture_q0115_09.htm";
						}
						else
						{
							st.giveItem(Tablet);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(12);
							return "ice_sculpture_q0115_08.htm";
						}
				}
			}
		}
		else if(npcId == Sculpture4)
		{
			if(st.isStarted())
			{
				switch(st.getMemoState())
				{
					case 7:
						if(st.getMemoStateEx(1) <= 7)
						{
							int memoStateEx = st.getMemoStateEx(1);
							if(memoStateEx == 3 || memoStateEx == 5 || memoStateEx == 6)
							{
								return "ice_sculpture_q0115_02.htm";
							}
							else if(memoStateEx == 7)
							{
								return "ice_sculpture_q0115_05.htm";
							}
							else
							{
								st.setMemoStateEx(1, memoStateEx + 8);
								return "ice_sculpture_q0115_01.htm";
							}
						}
						else
						{
							return "ice_sculpture_q0115_01a.htm";
						}
					case 8:
						return "ice_sculpture_q0115_07.htm";
					case 11:
						if(st.hasQuestItems(Tablet))
						{
							return "ice_sculpture_q0115_09.htm";
						}
						else
						{
							st.giveItem(Tablet);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(12);
							return "ice_sculpture_q0115_08.htm";
						}
				}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}