package dwo.scripts.npc.hellbound;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.01.13
 * Time: 0:55
 */

public class Solomon extends Quest
{
	private static final int NPC = 32355;

	public Solomon()
	{
		addFirstTalkId(NPC);
	}

	public static void main(String[] args)
	{
		new Solomon();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int hellboundLevel = HellboundManager.getInstance().getLevel();
		if(hellboundLevel == 5)
		{
			return "solmon001.htm";
		}
		if(hellboundLevel > 5)
		{
			return "solmon001a.htm";
		}
		return null;
	}
}