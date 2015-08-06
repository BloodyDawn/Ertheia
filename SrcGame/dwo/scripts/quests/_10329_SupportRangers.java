package dwo.scripts.quests;

import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.11
 * Time: 22:35
 */

public class _10329_SupportRangers extends Quest
{
	private static final int KEKIY = 30565;
	private static final int ATRAN = 33448;
	private static final int BAT = 33204;

	public _10329_SupportRangers()
	{
		addStartNpc(KEKIY);
		addTalkId(KEKIY, ATRAN);
	}

	public static void main(String[] args)
	{
		new _10329_SupportRangers();
	}

	@Override
	public int getQuestId()
	{
		return 10329;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			L2Npc batWalker = addSpawn(BAT, qs.getPlayer());
			batWalker.broadcastPacket(new NS(batWalker.getObjectId(), ChatType.NPC_ALL, batWalker.getNpcId(), 1811264).addStringParameter(qs.getPlayer().getName()));
			WalkingManager.getInstance().startMoving(batWalker, 18);
			return "kakai_the_lord_of_flame_q10329_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == KEKIY)
		{
			if(reply == 1)
			{
				return "kakai_the_lord_of_flame_q10329_02.htm";
			}
		}
		else if(npc.getNpcId() == ATRAN)
		{
			if(reply == 2)
			{
				st.giveAdena(25000, true);
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(11022201), ExShowScreenMessage.MIDDLE_CENTER, 4000));
				st.giveItems(875, 2);
				st.giveItems(906, 1);
				st.addExpAndSp(16900, 5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_atran_q10329_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == KEKIY)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "kakai_the_lord_of_flame_q10329_04.htm";
				case CREATED:
					if(canBeStarted(player))
					{
						return "kakai_the_lord_of_flame_q10329_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "kakai_the_lord_of_flame_q10329_03.htm";
					}
				case STARTED:
					return "kakai_the_lord_of_flame_q10329_06.htm";
			}
		}
		else if(npc.getNpcId() == ATRAN)
		{
			return st.isCompleted() ? "si_illusion_atran_q10329_03.htm" : "si_illusion_atran_q10329_01.htm";
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10328_PleaseSealThePartOfTheAncientEvil.class);
		return previous != null && previous.isCompleted() && player.getLevel() <= 20;

	}
}
