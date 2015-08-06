package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.scripts.instances.FQ_JiniaGuildHideout;
import dwo.scripts.instances.FQ_MithrilMine;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.11.12
 * Time: 20:40
 */

public class _10284_AcquisitionOfDivineSword extends Quest
{
	// Квестовые персонажи
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int KRUN = 32653;
	private static final int TARUN = 32654;
	private static final int KEGOR = 18846;

	public _10284_AcquisitionOfDivineSword()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, KRUN, TARUN, KEGOR);
	}

	public static void main(String[] args)
	{
		new _10284_AcquisitionOfDivineSword();
	}

	@Override
	public int getQuestId()
	{
		return 10284;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(1);
			return "repre_q10284_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == RAFFORTY)
		{
			if(reply == 1)
			{
				FQ_JiniaGuildHideout.getInstance().enterInstance(player);
				return null;
			}
		}
		else if(npc.getNpcId() == JINIA)
		{
			if(st.isStarted())
			{
				switch(reply)
				{
					case 1:
						if(st.getMemoState() == 1)
						{
							if(st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 0 && st.getMemoStateEx(3) == 0)
							{
								return "jinia_npc_q10284_05a.htm";
							}
							else if(st.getMemoStateEx(1) == 0 && st.getMemoStateEx(2) == 1 && st.getMemoStateEx(3) == 0)
							{
								return "jinia_npc_q10284_05b.htm";
							}
							else if(st.getMemoStateEx(1) == 0 && st.getMemoStateEx(2) == 0 && st.getMemoStateEx(3) == 1)
							{
								return "jinia_npc_q10284_05c.htm";
							}
							else if(st.getMemoStateEx(1) == 0 && st.getMemoStateEx(2) == 1 && st.getMemoStateEx(3) == 1)
							{
								return "jinia_npc_q10284_05d.htm";
							}
							else if(st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 0 && st.getMemoStateEx(3) == 1)
							{
								return "jinia_npc_q10284_05e.htm";
							}
							else if(st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 1 && st.getMemoStateEx(3) == 0)
							{
								return "jinia_npc_q10284_05f.htm";
							}
							else if(st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 1 && st.getMemoStateEx(3) == 1)
							{
								return "jinia_npc_q10284_05g.htm";
							}
						}
						break;
					case 10:
						if(st.getMemoState() == 1)
						{
							return "jinia_npc_q10284_02a.htm";
						}
						break;
					case 11:
						if(st.getMemoState() == 1)
						{
							return "jinia_npc_q10284_02b.htm";
						}
						break;
					case 12:
						if(st.getMemoState() == 1)
						{
							st.setMemoStateEx(1, 1);
							return "jinia_npc_q10284_02c.htm";
						}
						break;
					case 20:
						if(st.getMemoState() == 1)
						{
							return "jinia_npc_q10284_03a.htm";
						}
						break;
					case 21:
						if(st.getMemoState() == 1)
						{
							return "jinia_npc_q10284_03b.htm";
						}
						break;
					case 22:
						if(st.getMemoState() == 1)
						{
							st.setMemoStateEx(2, 1);
							return "jinia_npc_q10284_03c.htm";
						}
						break;
					case 30:
						if(st.getMemoState() == 1)
						{
							return "jinia_npc_q10284_04a.htm";
						}
						break;
					case 31:
						if(st.getMemoState() == 1)
						{
							return "jinia_npc_q10284_04b.htm";
						}
						break;
					case 32:
						if(st.getMemoState() == 1)
						{
							st.setMemoStateEx(3, 1);
							return "jinia_npc_q10284_04c.htm";
						}
						break;
					case 2:
						if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 1 && st.getMemoStateEx(3) == 1)
						{
							return "jinia_npc_q10284_06.htm";
						}
						break;
					case 3:
						if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 1 && st.getMemoStateEx(3) == 1)
						{
							st.setMemoStateEx(1, 0);
							st.setMemoStateEx(2, 0);
							st.setMemoStateEx(3, 0);
							st.setMemoState(2);
							st.setCond(3);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(60);
							return "jinia_npc_q10284_07.htm";
						}
						break;
					case 4:
						if(st.getMemoState() == 2)
						{
							InstanceManager.getInstance().destroyInstance(player.getInstanceId());
						}
						break;
				}
			}
		}
		else if(npc.getNpcId() == KEGOR)
		{
			if(reply == 2)
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				return null;
			}
		}
		else if(npc.getNpcId() == KRUN)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 2)
				{
					return "kroon_q10284_02.htm";
				}
			}
			else if(reply == 2)
			{
				FQ_MithrilMine.getInstance().enterInstance(player);
				return null;
			}
			else if(reply == 3)
			{
				if(st.getMemoState() == 2)
				{
					return "kroon_q10284_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == TARUN)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 2)
				{
					return "taroon_q10284_02.htm";
				}
			}
			else if(reply == 2)
			{
				FQ_MithrilMine.getInstance().enterInstance(player);
				return null;
			}
			else if(reply == 3)
			{
				if(st.getMemoState() == 2)
				{
					return "taroon_q10284_03.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		L2PcInstance player = st.getPlayer();

		if(npcId == RAFFORTY)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "repre_q10284_02.htm";
				case CREATED:
					QuestState pst = player.getQuestState(_10283_RequestOfIceMerchant.class);
					return player.getLevel() >= 82 && pst != null && pst.isCompleted() ? "repre_q10284_01.htm" : "repre_q10284_03.htm";
				case STARTED:
					switch(st.getMemoState())
					{
						case 1:
							return "repre_q10284_05.htm";
						case 2:
							return "repre_q10284_09.htm";
					}
			}
		}
		else if(npcId == KRUN)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 2)
				{
					return "kroon_q10284_01.htm";
				}
				else if(st.getMemoState() == 3)
				{
					st.giveAdena(296425, true);
					st.addExpAndSp(921805, 82230);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "kroon_q10284_05.htm";
				}
			}
		}
		else if(npcId == TARUN)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 2)
				{
					return "taroon_q10284_01.htm";
				}
				else if(st.getMemoState() == 3)
				{
					st.giveAdena(296425, true);
					st.addExpAndSp(921805, 82230);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "taroon_q10284_05.htm";
				}
			}
		}
		else if(npcId == JINIA)
		{
			if(st.isStarted())
			{
				return "taroon_q10284_01.htm";
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10283_RequestOfIceMerchant.class);
		return !(player.getLevel() < 82 || pst == null || !pst.isCompleted());
	}
}