package dwo.scripts.services.validator;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.12.12
 * Time: 17:43
 */

public class OlympiadItemsValidator extends Quest
{
	public OlympiadItemsValidator()
	{
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new OlympiadItemsValidator();
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(!player.getOlympiadController().isHero())
		{
			for(int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
			{
				L2ItemInstance equippedItem = player.getInventory().getPaperdollItem(i);
				if(equippedItem != null && equippedItem.isHeroItem())
				{
					player.getInventory().unEquipItemInSlot(i);
				}
			}

			for(L2ItemInstance item : player.getInventory().getAvailableItems(false, true, false))
			{
				if(item != null && item.isHeroItem())
				{
					player.destroyItem(ProcessType.HERO, item, null, true);
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(item);
					player.sendPacket(iu);
				}
			}
		}
	}
}