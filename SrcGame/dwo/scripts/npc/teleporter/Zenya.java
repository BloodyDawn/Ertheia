package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.09.12
 * Time: 20:10
 */

public class Zenya extends Quest
{
	private static final int Zenya = 32140;

	public Zenya()
	{
		addAskId(Zenya, -922);
	}

	public static void main(String[] args)
	{
		new Zenya();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			return "subelder_zenya002.htm";
		}
		if(reply == 2)
		{
			if(player.getLevel() < 80)
			{
				return "subelder_zenya003.htm";
			}
			else
			{
				player.teleToLocation(183399, -81012, -5320);
			}
		}
		return null;
	}
}