package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.09.12
 * Time: 20:17
 */

public class Alexandria extends Quest
{
	private static final int AlexandriaNPC = 30098;

	private static final int BIG_RED_NIBLE_FISH = 6471;
	private static final int GREAT_CODRAN = 5094;
	private static final int MEMENTO_MORI = 9814;
	private static final int EARTH_EGG = 9816;
	private static final int NONLIVING_NUCLEUS = 9817;
	private static final int DRAGON_HEART = 9815;

	public Alexandria()
	{
		addAskId(AlexandriaNPC, -1901);
	}

	public static void main(String[] args)
	{
		new Alexandria();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			if(hasAllItemsForExchange(player))
			{
				player.destroyItemByItemId(ProcessType.NPC, 6471, 25, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 5094, 50, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9814, 4, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9815, 3, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9816, 5, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9817, 5, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, 7500000, npc, true);

				if(Rnd.getChance(20))
				{
					player.addItem(ProcessType.NPC, Rnd.get(10322, 10326), 1, npc, true);
					return "alexandria003a.htm";
				}
				else
				{
					player.addItem(ProcessType.NPC, 10408, 1, npc, true);
					player.addItem(ProcessType.NPC, 10321, 1, npc, true);
					return "alexandria003.htm";
				}
			}
			else
			{
				return "alexandria004.htm";
			}
		}
		if(reply == 2)
		{
			if(hasAllItemsForExchange(player))
			{
				player.destroyItemByItemId(ProcessType.NPC, 6471, 25, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 5094, 50, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9814, 4, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9815, 3, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9816, 5, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, 9817, 5, npc, true);
				player.destroyItemByItemId(ProcessType.NPC, PcInventory.ADENA_ID, 7500000, npc, true);

				if(Rnd.getChance(20))
				{
					player.addItem(ProcessType.NPC, Rnd.get(10316, 10320), 1, npc, true);
					return "alexandria003a.htm";
				}
				else
				{
					player.addItem(ProcessType.NPC, 10408, 1, npc, true);
					player.addItem(ProcessType.NPC, 10315, 1, npc, true);
					return "alexandria003.htm";
				}
			}
			else
			{
				return "alexandria004.htm";
			}
		}
		return null;
	}

	/**
	 * @param pc L2PcInstance персонажа
	 * @return {@code true} если у персонажа есть все необходимые предметы
	 */
	private boolean hasAllItemsForExchange(L2PcInstance pc)
	{
		PcInventory inventory = pc.getInventory();
		return inventory.getCountOf(BIG_RED_NIBLE_FISH) >= 25 && inventory.getCountOf(GREAT_CODRAN) >= 50 && inventory.getCountOf(MEMENTO_MORI) >= 4 && inventory.getCountOf(EARTH_EGG) >= 5 && inventory.getCountOf(NONLIVING_NUCLEUS) >= 5 && inventory.getCountOf(DRAGON_HEART) >= 3 && inventory.getAdenaCount() >= 7500000;
	}
}