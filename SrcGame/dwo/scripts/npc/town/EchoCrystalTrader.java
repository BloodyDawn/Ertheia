package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.01.13
 * Time: 23:12
 */

public class EchoCrystalTrader extends Quest
{
	private static final int[] NPCs = {
		31042, 31043
	};

	public EchoCrystalTrader()
	{
		addAskId(NPCs, 362);
	}

	public static void main(String[] args)
	{
		new EchoCrystalTrader();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 362)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(4410) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4411, 1, true);
					return npc.getServerName() + "_q0362_01.htm";
				}
				else if(player.getItemsCount(4410) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_02.htm";
				}
				else if(player.getItemsCount(4410) == 0)
				{
					return npc.getServerName() + "q0362_03.htm";
				}
			}
			if(reply == 2)
			{
				if(player.getItemsCount(4409) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4412, 1, true);
					return npc.getServerName() + "q0362_04.htm";
				}
				else if(player.getItemsCount(4409) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_05.htm";
				}
				else if(player.getItemsCount(4409) == 0)
				{
					return npc.getServerName() + "q0362_06.htm";
				}
			}
			if(reply == 3)
			{
				if(player.getItemsCount(4408) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4413, 1, true);
					return npc.getServerName() + "q0362_07.htm";
				}
				else if(player.getItemsCount(4408) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_08.htm";
				}
				else if(player.getItemsCount(4408) == 0)
				{
					return npc.getServerName() + "q0362_09.htm";
				}
			}
			if(reply == 4)
			{
				if(player.getItemsCount(4420) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4414, 1, true);
					return npc.getServerName() + "q0362_10.htm";
				}
				else if(player.getItemsCount(4420) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_11.htm";
				}
				else if(player.getItemsCount(4420) == 0)
				{
					return npc.getServerName() + "q0362_12.htm";
				}
			}
			if(reply == 5)
			{
				if(player.getItemsCount(4421) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4415, 1, true);
					return npc.getServerName() + "q0362_13.htm";
				}
				else if(player.getItemsCount(4421) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_14.htm";
				}
				else if(player.getItemsCount(4421) == 0)
				{
					return npc.getServerName() + "q0362_15.htm";
				}
			}
			if(reply == 6)
			{
				if(player.getItemsCount(4419) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4417, 1, true);
					return npc.getServerName() + "q0362_16.htm";
				}
				else if(player.getItemsCount(4419) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_05.htm";
				}
				else if(player.getItemsCount(4419) == 0)
				{
					return npc.getServerName() + "q0362_06.htm";
				}
			}
			if(reply == 7)
			{
				if(player.getItemsCount(4418) > 0 && player.getAdenaCount() >= 200)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, PcInventory.ADENA_ID, 200, 4416, 1, true);
					return npc.getServerName() + "q0362_17.htm";
				}
				else if(player.getItemsCount(4418) > 0 && player.getAdenaCount() < 200)
				{
					return npc.getServerName() + "q0362_05.htm";
				}
				else if(player.getItemsCount(4418) == 0)
				{
					return npc.getServerName() + "q0362_06.htm";
				}
			}
		}
		return null;
	}
}