package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 04.02.13
 * Time: 12:18
 */
public class GatekeeperRamsedas extends Quest
{
	private static final int NPC = 32614;

	public GatekeeperRamsedas()
	{
		addAskId(NPC, -1056);
		addAskId(NPC, -1057);
	}

	public static void main(String[] args)
	{
		new GatekeeperRamsedas();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -1056:
				if(reply == 0)
				{
					player.teleToLocation(-112383, 256848, -1469);
					return null;
				}
			case -1057:
				if(reply == 0)
				{
					player.teleToLocation(-14513, 123941, -3122);
					return null;
				}
		}
		return null;
	}
}
