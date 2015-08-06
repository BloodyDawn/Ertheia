package dwo.scripts.npc;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._10285_MeetingSirra;
import dwo.scripts.quests._10286_ReunionWithSirra;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.11.12
 * Time: 23:37
 */

public class Sirra extends Quest
{
	private static final int SIRRA = 32762;

	private static final int[] TEMPLATE_IDS = {
		InstanceZoneId.ICE_QUEENS_CASTLE_2.getId(), InstanceZoneId.ICE_QUEENS_CASTLE_ULTIMATE_BATTLE.getId()
	};

	public Sirra()
	{
		addFirstTalkId(SIRRA);
		addAskId(SIRRA, -2316);
	}

	public static void main(String[] args)
	{
		new Sirra();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == SIRRA)
		{
			if(ask == -2316)
			{
				if(reply == 1)
				{
					player.teleToLocation(114694, -113700, -11200);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getNpcId() == SIRRA)
		{
			InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(world != null && ArrayUtils.contains(TEMPLATE_IDS, world.templateId))
			{
				return world.status > 1 ? "sirr_npc001.htm" : "sirr_npc002.htm";
			}
			else
			{
				QuestState st = player.getQuestState(_10285_MeetingSirra.class);
				if(st != null && st.getMemoState() == 1)
				{
					switch(st.getMemoStateEx(1))
					{
						case 3:
							return "sirr_npc_q10285_01.htm";
						case 4:
							return "sirr_npc_q10285_09.htm";
					}
				}
				st = player.getQuestState(_10286_ReunionWithSirra.class);
				if(st != null && st.getMemoState() == 1)
				{
					switch(st.getMemoStateEx(1))
					{
						case 1:
							return "sirr_npc_q10286_01.htm";
						case 2:
							return "sirr_npc_q10286_05.htm";
					}
				}
			}
		}
		return super.onFirstTalk(npc, player);
	}
}