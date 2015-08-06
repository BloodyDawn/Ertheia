package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.13
 * Time: 0:35
 */

public class RuinProphet extends Quest
{
	private static final int[] NPCs = {4310, 4311, 4312};

	public RuinProphet()
	{
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new RuinProphet();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "ruin_prophet_gludio001.htm";
	}
}
