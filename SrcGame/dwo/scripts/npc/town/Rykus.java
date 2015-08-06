package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.13
 * Time: 15:24
 */

public class Rykus extends Quest
{
	private static final int NPC = 33521;

	// Предметы
	private static final int Shield = 17724;
	private static final int ShieldImproved = 17723;

	public Rykus()
	{
		addAskId(NPC, 100);
	}

	public static void main(String[] args)
	{
		new Rykus();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == NPC)
		{
			if(ask == 100)
			{
				if(reply == 1)
				{
					if(player.getItemsCount(Shield) > 0)
					{
						if(player.getAdenaCount() >= 5000)
						{
							player.exchangeItemsById(ProcessType.NPC, npc, Shield, 1, ShieldImproved, 1, true);
							player.getInventory().reduceAdena(ProcessType.NPC, 5000, player, npc);
							return "orbis_rykus009.htm";
						}
						else
						{
							return "orbis_rykus011.htm";
						}
					}
					else
					{
						return "orbis_rykus012.htm";
					}
				}
				else if(reply == 2)
				{
					long shieldsCount = player.getItemsCount(Shield);
					long adenaCount = player.getAdenaCount();

					if(shieldsCount > 0)
					{
						long availableShields = adenaCount / 5000;
						if(availableShields > 0)
						{
							player.exchangeItemsById(ProcessType.NPC, npc, Shield, availableShields, ShieldImproved, availableShields, true);
							player.getInventory().reduceAdena(ProcessType.NPC, 5000 * availableShields, player, npc);
							return "orbis_rykus010.htm";
						}
						else
						{
							return "orbis_rykus011.htm";
						}
					}
					else
					{
						return "orbis_rykus012.htm";
					}
				}
			}
		}
		return null;
	}
}