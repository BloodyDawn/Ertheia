package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.scripts.instances.FQ_IceQueenCastle;
import dwo.scripts.instances.FQ_SirraMeeting_Q10285;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.11.12
 * Time: 23:37
 */

public class _10285_MeetingSirra extends Quest
{
	// Квестовые персонажи
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int JINIA2 = 32781;
	private static final int KEGOR = 32761;
	private static final int SIRRA = 32762;

	public _10285_MeetingSirra()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, KEGOR, SIRRA, JINIA2);
	}

	public static void main(String[] args)
	{
		new _10285_MeetingSirra();
	}

	@Override
	public int getQuestId()
	{
		return 10285;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(1);
			qs.setMemoStateEx(1, 0);
			return "repre_q10285_05.htm";
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
				FQ_SirraMeeting_Q10285.getInstance().enterInstance(player);
				return null;
			}
			else if(reply == 2)
			{
				return "repre_q10285_04.htm";
			}
		}
		else if(npc.getNpcId() == JINIA)
		{
			switch(reply)
			{
				case 1:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
					{
						st.setMemoStateEx(1, 1);
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10285_02.htm";
					}
					break;
				case 11:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 2)
					{
						return "jinia_npc_q10285_05.htm";
					}
					break;
				case 12:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 2)
					{
						L2Npc sirra = addSpawn(SIRRA, -23905, -8790, -5384, 56238, false, 60000, false, player.getInstanceId());
						sirra.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.THERES_NOTHING_YOU_CANT_SAY_I_CANT_LISTEN_TO_YOU_ANYMORE));
						st.setMemoStateEx(1, 3);
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10285_06.htm";
					}
					break;
				case 21:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 4)
					{
						return "jinia_npc_q10285_09.htm";
					}
					break;
				case 22:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 4)
					{
						return "jinia_npc_q10285_10.htm";
					}
					break;
				case 23:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 4)
					{
						return "jinia_npc_q10285_11.htm";
					}
					break;
				case 24:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 4)
					{
						st.setMemoStateEx(1, 5);
						st.setCond(7);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10285_12.htm";
					}
					break;
				case 25:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 5)
					{
						st.setMemoStateEx(1, 0);
						st.setMemoState(2);
						st.setCond(7);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(60);
						return "jinia_npc_q10285_14.htm";
					}
					break;
				case 26:
					if(st.getMemoState() == 2)
					{
						InstanceManager.getInstance().destroyInstance(player.getInstanceId());
						return "jinia_npc_q10285_15.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == KEGOR)
		{
			if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1)
			{
				st.setMemoStateEx(1, 2);
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "kegor_npc_q10285_02.htm";
			}
		}
		else if(npc.getNpcId() == SIRRA)
		{
			switch(reply)
			{
				case 1:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						return "sirr_npc_q10285_02.htm";
					}
					break;
				case 2:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						return "sirr_npc_q10285_03.htm";
					}
					break;
				case 3:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						return "sirr_npc_q10285_04.htm";
					}
					break;
				case 4:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						return "sirr_npc_q10285_05.htm";
					}
					break;
				case 5:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						return "sirr_npc_q10285_06.htm";
					}
					break;
				case 6:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						return "sirr_npc_q10285_07.htm";
					}
					break;
				case 7:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 3)
					{
						st.setMemoStateEx(1, 4);
						st.setCond(6);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "sirr_npc_q10285_08.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == JINIA2)
		{
			if(st.getCond() == 8 || st.getCond() == 9)
			{
				if(reply == 1)
				{
					return "jinia_npc2_q10285_02.htm";
				}
				else if(reply == 2)
				{
					FQ_IceQueenCastle.getInstance().enterInstance(player);
					return null;
				}
				else if(reply == 3)
				{
					return "jinia_npc2_q10285_03.htm";
				}
			}
			else
			{
				return "jinia_npc2_q10285_08.htm";
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
					return "repre_q10285_02.htm";
				case CREATED:
					QuestState pst = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
					return player.getLevel() >= 82 && pst != null && pst.isCompleted() ? "repre_q10285_01.htm" : "repre_q10285_03.htm";
				case STARTED:
					switch(st.getMemoState())
					{
						case 1:
							return "repre_q10285_06.htm";
						case 2:
							return "repre_q10285_09.htm";
						case 3:
							st.giveAdena(283425, true);
							st.addExpAndSp(939075, 83855);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "repre_q10285_10.htm";
					}
			}
		}
		else if(npcId == KEGOR)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 1)
				{
					switch(st.getMemoStateEx(1))
					{
						case 1:
							return "kegor_npc_q10285_01.htm";
						case 2:
							return "kegor_npc_q10285_03.htm";
						case 3:
							return "kegor_npc_q10285_04.htm";
					}
				}
			}
		}
		else if(npcId == SIRRA)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 1)
				{
					switch(st.getMemoStateEx(1))
					{
						case 3:
							return "sirr_npc_q10285_01.htm";
						case 4:
							return "sirr_npc_q10285_09.htm";
					}
				}
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
		return !(player.getLevel() < 82 || pst == null || !pst.isCompleted());
	}
}