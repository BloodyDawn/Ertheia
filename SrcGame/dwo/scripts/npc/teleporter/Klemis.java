package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.05.12
 * Time: 23:37
 */

public class Klemis extends Quest
{
	private static final int KlemisNPC = 32734;

	public Klemis()
	{
		addAskId(KlemisNPC, -415);
	}

	public static void main(String[] args)
	{
		new Klemis();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			if(player.getLevel() < 85)
			{
				return "clemis002.htm";
			}
			else
			{
				player.teleToLocation(-180218, 185923, -10576);
			}
		}
		return null;
	}
}