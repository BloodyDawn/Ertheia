package dwo.scripts.quests;

import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.NS;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.11
 * Time: 22:57
 */

public class _10326_RespectToTheOldMan extends Quest
{
	private static final int GALLINT = 32980;
	private static final int PANTEON = 32972;
	private static final int MONKEY = 32971;

	public _10326_RespectToTheOldMan()
	{
		addStartNpc(GALLINT);
		addTalkId(GALLINT, PANTEON);
	}

	public static void main(String[] args)
	{
		new _10326_RespectToTheOldMan();
	}

	@Override
	public int getQuestId()
	{
		return 10326;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_galint_new_q10326_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == GALLINT)
		{
			if(reply == 1 && !st.getBool("monkey_started"))
			{
				L2Npc monkeyWalker = addSpawn(MONKEY, npc);
				monkeyWalker.broadcastPacket(new NS(monkeyWalker.getObjectId(), ChatType.NPC_ALL, monkeyWalker.getNpcId(), 1032307).addStringParameter(player.getName()));
				WalkingManager.getInstance().startMoving(monkeyWalker, 17);
				st.set("monkey_started", "true");
				return "si_galint_new_q10326_04.htm";
			}
		}
		else if(npc.getNpcId() == PANTEON)
		{
			if(reply == 1)
			{
				st.giveAdena(14000, true);
				st.addExpAndSp(5300, 5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_pantheon_q10326_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prevst = player.getQuestState(_10325_InSearchOfNewForces.class);

		if(npc.getNpcId() == GALLINT)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					if(prevst != null && prevst.isCompleted())
					{
						return "si_galint_new_q10326_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_galint_new_q10326_02.htm";
					}
				case STARTED:
					return "si_galint_new_q10326_06.htm";
			}
		}
		else if(npc.getNpcId() == PANTEON)
		{
			if(st.isCompleted())
			{
				return "si_illusion_pantheon_q10326_02.htm";
			}
			else if(st.getCond() == 1)
			{
				return "si_illusion_pantheon_q10326_03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10325_InSearchOfNewForces.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;
	}
}
