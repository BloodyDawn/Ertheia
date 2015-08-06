package dwo.scripts.npc.hellbound;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.HB_HellboundTown;

public class Kanaf extends Quest
{
	private static final int KANAF = 32346;

	public Kanaf()
	{
		addAskId(KANAF, -1006);
	}

	public static void main(String[] args)
	{
		new Kanaf();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				if(HellboundManager.getInstance().getLevel() >= 10)
				{
					if(player.getParty() != null)
					{
						HB_HellboundTown.getInstance().enterInstance(player);
					}
					else
					{
						return "kcaien002b.htm";
					}
				}
				else
				{
					return "kcaien002a.htm";
				}
			}
			else if(reply == 2)
			{
				switch(Rnd.get(2))
				{
					case 0:
						return "kcaien001a.htm";
					case 1:
						return "kcaien001b.htm";
					case 2:
						return "kcaien001c.htm";
				}
			}
		}
		return null;
	}
}