package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.11.11
 * Time: 0:59
 */

public class MammonBlackMarketeer extends Quest
{
	// Контрабандист Маммона
	private static final int _MammonBlackMarketeer = 31092;

	public MammonBlackMarketeer()
	{
		addAskId(_MammonBlackMarketeer, 506);
	}

	public static void main(String[] args)
	{
		new MammonBlackMarketeer();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 506)
		{
			if(reply == 3)
			{
				long ancientAdenaCount = player.getItemsCount(PcInventory.ANCIENT_ADENA_ID);
				if(ancientAdenaCount > 0)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ANCIENT_ADENA_ID, ancientAdenaCount, PcInventory.ADENA_ID, ancientAdenaCount << 2, true);
					return "marketeer_of_mammon004.htm";
				}
			}
		}
		return null;
	}
}