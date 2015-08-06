package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.scripts.instances.FQ_SirraMeeting_Q10286;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.11.12
 * Time: 23:37
 */

public class _10286_ReunionWithSirra extends Quest
{
	// Квестовые персонажи
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int JINIA2 = 32781;
	private static final int SIRRA = 32762;

	// Квестовые предметы
	private static final int FROZEN_CORE = 15470;

	public _10286_ReunionWithSirra()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, JINIA2, SIRRA);
	}

	public static void main(String[] args)
	{
		new _10286_ReunionWithSirra();
	}

	@Override
	public int getQuestId()
	{
		return 10286;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(1);
			qs.setMemoStateEx(1, 0);
			return "repre_q10286_04.htm";
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
				FQ_SirraMeeting_Q10286.getInstance().enterInstance(player);
				return null;
			}
			else if(reply == 2)
			{
				return "repre_q10286_05.htm";
			}
		}
		else if(npc.getNpcId() == JINIA)
		{
			switch(reply)
			{
				case 1:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
					{
						return "jinia_npc_q10286_02.htm";
					}
					break;
				case 2:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
					{
						return "jinia_npc_q10286_03.htm";
					}
					break;
				case 3:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
					{
						return "jinia_npc_q10286_04.htm";
					}
					break;
				case 4:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
					{
						L2Npc sirra = addSpawn(SIRRA, -23905, -8790, -5384, 56238, false, 60000, false, player.getInstanceId());
						sirra.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.YOU_ADVANCED_BRAVELY_BUT_GOT_SUCH_A_TINY_RESULT_HOHOHO));
						st.setMemoStateEx(1, 1);
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10286_06.htm";
					}
					break;
				case 11:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 2)
					{
						st.setMemoState(2);
						InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(60);
						return "jinia_npc_q10286_09.htm";
					}
					break;
				case 12:
					if(st.getMemoState() == 2)
					{
						InstanceManager.getInstance().destroyInstance(player.getInstanceId());
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10286_10.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == SIRRA)
		{
			switch(reply)
			{
				case 1:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1)
					{
						return "sirr_npc_q10286_02.htm";
					}
					break;
				case 2:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1)
					{
						return "sirr_npc_q10286_03.htm";
					}
					break;
				case 3:
					if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1)
					{
						if(!st.hasQuestItems(15470))
						{
							st.giveItems(15470, 5);
						}
						st.setCond(4);
						st.setMemoStateEx(1, 2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "sirr_npc_q10286_04.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == JINIA2)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 2)
				{
					return "jinia_npc2_q10286_02.htm";
				}
			}
			if(reply == 3)
			{
				if(st.getMemoState() == 2)
				{
					return "jinia_npc2_q10286_03.htm";
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
					return "repre_q10286_02.htm";
				case CREATED:
					QuestState pst = player.getQuestState(_10285_MeetingSirra.class);
					return player.getLevel() >= 82 && pst != null && pst.isCompleted() ? "repre_q10286_01.htm" : "repre_q10286_03.htm";
				case STARTED:
					switch(st.getMemoState())
					{
						case 1:
							return "repre_q10286_06.htm";
						case 2:
							return "repre_q10286_09.htm";
					}
			}
		}
		else if(npcId == SIRRA)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 1)
				{
					if(st.getMemoStateEx(1) == 1)
					{
						return "sirr_npc_q10286_01.htm";
					}
					else if(st.getMemoStateEx(1) == 2)
					{
						return "sirr_npc_q10286_05.htm";
					}
				}
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10285_MeetingSirra.class);
		return !(player.getLevel() < 82 || pst == null || !pst.isCompleted());
	}
}