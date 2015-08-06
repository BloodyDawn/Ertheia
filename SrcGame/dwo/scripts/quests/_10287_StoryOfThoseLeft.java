package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.scripts.instances.FQ_SirraMeeting_Q10287;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.11.12
 * Time: 22:37
 */

public class _10287_StoryOfThoseLeft extends Quest
{
	// Квестовые персонажи
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int KEGOR = 32761;

	public _10287_StoryOfThoseLeft()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, KEGOR);
	}

	public static void main(String[] args)
	{
		new _10287_StoryOfThoseLeft();
	}

	@Override
	public int getQuestId()
	{
		return 10287;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.setMemoState(1);
			qs.setMemoStateEx(1, 0);
			return "repre_q10287_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(npc.getNpcId() == RAFFORTY)
		{
			switch(reply)
			{
				case 1:
					FQ_SirraMeeting_Q10287.getInstance().enterInstance(player);
					break;
				case 2:
					if(st.getMemoState() == 2)
					{
						return "repre_q10287_10.htm";
					}
					break;
				case 11:
					if(st.getMemoState() == 2)
					{
						st.giveItems(10549, 1);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "repre_q10287_11.htm";
					}
					break;
				case 12:
					if(st.getMemoState() == 2)
					{
						st.giveItems(10550, 1);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "repre_q10287_11.htm";
					}
					break;
				case 13:
					if(st.getMemoState() == 2)
					{
						st.giveItems(10551, 1);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "repre_q10287_11.htm";
					}
					break;
				case 14:
					if(st.getMemoState() == 2)
					{
						st.giveItems(10552, 1);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "repre_q10287_11.htm";
					}
					break;
				case 15:
					if(st.getMemoState() == 2)
					{
						st.giveItems(10553, 1);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "repre_q10287_11.htm";
					}
					break;
				case 16:
					if(st.getMemoState() == 2)
					{
						st.giveItems(14219, 1);
						st.exitQuest(QuestType.ONE_TIME);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "repre_q10287_11.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == JINIA)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
				{
					if(world != null && world.templateId == 146)
					{
						return "jinia_npc_q10287_02.htm";
					}
				}
			}
			else if(reply == 2)
			{
				if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 0)
				{
					if(world != null && world.templateId == 146)
					{
						st.setMemoStateEx(1, 1);
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10287_03.htm";
					}
				}
			}
			else if(reply == 3)
			{
				if(st.getMemoState() == 2)
				{
					if(world != null && world.templateId == 146)
					{
						InstanceManager.getInstance().destroyInstance(player.getInstanceId());
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "jinia_npc_q10287_06.htm";
					}
				}
			}
		}
		else if(npc.getNpcId() == KEGOR)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 0)
				{
					if(world != null && world.templateId == 146)
					{
						return "kegor_q10287_03.htm";
					}
				}
			}
			if(reply == 2)
			{
				if(st.getMemoState() == 1 && st.getMemoStateEx(1) == 1 && st.getMemoStateEx(2) == 0)
				{
					if(world != null && world.templateId == 146)
					{
						st.setMemoStateEx(2, 1);
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "kegor_q10287_04.htm";
					}
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
					return "repre_q10287_02.htm";
				case CREATED:
					QuestState pst = player.getQuestState(_10286_ReunionWithSirra.class);
					return pst != null && pst.isCompleted() && player.getLevel() >= 82 ? "repre_q10287_01.htm" : "repre_q10287_03.htm";
				case STARTED:
					switch(st.getMemoState())
					{
						case 1:
							return "repre_q10287_05.htm";
						case 2:
							return "repre_q10287_09.htm";
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
						case 0:
							if(st.getMemoStateEx(2) == 0)
							{
								return "kegor_q10287_02.htm";
							}
						case 1:
							if(st.getMemoStateEx(2) == 0)
							{
								return "kegor_q10287_01.htm";
							}
							else if(st.getMemoStateEx(2) == 1)
							{
								return "kegor_q10287_05.htm";
							}
					}
				}
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10286_ReunionWithSirra.class);
		return !(player.getLevel() < 82 || pst == null || !pst.isCompleted());
	}
}