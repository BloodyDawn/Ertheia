package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 17:18
 */
public class CatOpera extends Quest
{
	private static final int NPC = 32972;

	public CatOpera()
	{
		addAskId(NPC, -1);
	}

	public static void main(String[] args)
	{
		new CatOpera();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -1:
				if(reply == 2)
				{
					player.teleToLocation(-111986, 257238, -1390);
					return null;
				}
		}
		return null;
	}
}
