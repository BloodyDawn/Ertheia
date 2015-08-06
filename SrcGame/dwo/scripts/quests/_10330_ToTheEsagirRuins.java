package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.11
 * Time: 22:03
 */

public class _10330_ToTheEsagirRuins extends Quest
{
	private static final int ATRAN = 33448;
	private static final int RAXIS = 32977;

	public _10330_ToTheEsagirRuins()
	{
		addStartNpc(ATRAN);
		addTalkId(ATRAN, RAXIS);
	}

	public static void main(String[] args)
	{
		new _10330_ToTheEsagirRuins();
	}

	@Override
	public int getQuestId()
	{
		return 10330;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "si_illusion_atran_q10330_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == ATRAN)
		{
			if(reply == 1)
			{
				return "si_illusion_atran_q10330_04.htm";
			}
		}
		else if(npc.getNpcId() == RAXIS)
		{
			if(st.isStarted())
			{
				if(reply == 1)
				{
					return "si_illusion_larcis_q10330_04.htm";
				}
				else if(reply == 2)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(11022202), ExShowScreenMessage.MIDDLE_CENTER, 4000));
					st.giveItems(22, 1);
					st.giveItems(29, 1);
					st.giveAdena(62000, true);
					st.addExpAndSp(23000, 5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "si_illusion_larcis_q10330_05.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == ATRAN)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_illusion_atran_q10330_03.htm";
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_atran_q10330_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_atran_q10330_02.htm";
					}
				case STARTED:
					return "si_illusion_atran_q10330_06.htm";
			}
		}
		else if(npc.getNpcId() == RAXIS)
		{
			if(st.isCompleted())
			{
				return "si_illusion_larcis_q10330_02.htm";
			}
			else if(st.getCond() == 1)
			{
				return "si_illusion_larcis_q10330_03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 8 && player.getLevel() <= 20;
	}
}
