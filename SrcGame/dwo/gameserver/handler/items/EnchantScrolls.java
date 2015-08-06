package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.enchant.item.ChooseInventoryItem;

public class EnchantScrolls implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;
		if(activeChar.isCastingNow())
		{
			return false;
		}

		if(activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
			return false;
		}

		activeChar.setActiveEnchantItem(item);
		activeChar.sendPacket(new ChooseInventoryItem(item.getItemId()));
		return true;
	}
}
