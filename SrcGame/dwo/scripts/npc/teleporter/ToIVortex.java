package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.05.12
 * Time: 23:37
 */

public class ToIVortex extends Quest
{
	// Персонажи
	private static final int[] Researchers = {30949, 30950, 30951};
	private static final int[] DimensionVortex = {30952, 30953, 30954};

	// Предметы
	private static final int BLUE_DIMENSION_STONE = 4402;
	private static final int GREEN_DIMENSION_STONE = 4401;
	private static final int RED_DIMENSION_STONE = 4403;

	public ToIVortex()
	{
		addAskId(DimensionVortex, -6);
		addAskId(Researchers, -6);
	}

	public static void main(String[] args)
	{
		new ToIVortex();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(Researchers, npc.getNpcId()))
		{
			if(!player.isInventoryUnder90(true))
			{
				return npc.getServerName() + "001c.htm";
			}
			switch(reply)
			{
				case 1:
					if(player.getInventory().getAdenaCount() >= 10000)
					{
						player.getInventory().destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, 10000, player, npc);
						player.getInventory().addItem(ProcessType.NPC, GREEN_DIMENSION_STONE, 1, player, npc, true);
						return null;
					}
					else
					{
						return npc.getServerName() + "001b.htm";
					}
				case 2:
					if(player.getInventory().getAdenaCount() >= 10000)
					{
						player.getInventory().destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, 10000, player, npc);
						player.getInventory().addItem(ProcessType.NPC, BLUE_DIMENSION_STONE, 1, player, npc, true);
						return null;
					}
					else
					{
						return npc.getServerName() + "001b.htm";
					}
				case 3:
					if(player.getInventory().getAdenaCount() >= 10000)
					{
						player.getInventory().destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, 10000, player, npc);
						player.getInventory().addItem(ProcessType.NPC, RED_DIMENSION_STONE, 1, player, npc, true);
						return null;
					}
					else
					{
						return npc.getServerName() + "001b.htm";
					}
			}
		}
		else if(ArrayUtils.contains(DimensionVortex, npc.getNpcId()))
		{
			switch(reply)
			{
				case 1:
					if(player.getInventory().getCountOf(GREEN_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, GREEN_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(114356, 13423, -5096);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 2:
					if(player.getInventory().getCountOf(GREEN_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, GREEN_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(114666, 13380, -3608);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 3:
					if(player.getInventory().getCountOf(GREEN_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, GREEN_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(111982, 16028, -2120);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 4:
					if(player.getInventory().getCountOf(BLUE_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, BLUE_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(114636, 13413, -640);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 5:
					if(player.getInventory().getCountOf(BLUE_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, BLUE_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(114152, 19902, 928);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 6:
					if(player.getInventory().getCountOf(BLUE_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, BLUE_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(117131, 16044, 1944);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 7:
					if(player.getInventory().getCountOf(RED_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, RED_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(113026, 17687, 2952);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 8:
					if(player.getInventory().getCountOf(RED_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, RED_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(115571, 13723, 3960);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 9:
					if(player.getInventory().getCountOf(RED_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, RED_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(114649, 14144, 4976);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
				case 10:
					if(player.getInventory().getCountOf(RED_DIMENSION_STONE) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, RED_DIMENSION_STONE, 1, npc, true);
						player.teleToLocation(118507, 16605, 5984);
						return null;
					}
					else
					{
						return npc.getServerName() + "005.htm";
					}
			}
		}
		return null;
	}
}
