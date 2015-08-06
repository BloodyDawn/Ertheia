package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.13
 * Time: 0:41
 */

public class BattleOfficer extends Quest
{
	private static final int NPC = 19154;

	public BattleOfficer()
	{
		addFirstTalkId(NPC);
		addAskId(NPC, -3532);
	}

	public static void main(String[] args)
	{
		new BattleOfficer();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == NPC)
		{
			if(ask == -3532)
			{
				if(reply == 1 && npc.getOwner() == null)
				{
					npc.setOwner(player);
					npc.setIsRunning(true);
					npc.getAI().startFollow(player);
					npc.startDefenceOwnerTask();
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getOwner() == null)
		{
			return "si_esagira_mil03001.htm";
		}
		return null;
	}
}