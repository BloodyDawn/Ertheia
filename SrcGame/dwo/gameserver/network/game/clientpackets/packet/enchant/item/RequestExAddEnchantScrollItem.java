package dwo.gameserver.network.game.clientpackets.packet.enchant.item;

import dwo.gameserver.datatables.xml.EnchantItemData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.EnchantScroll;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.enchant.item.ExPutEnchantScrollItemResult;

/**
 * User: Bacek
 * Date: 07.02.13
 * Time: 4:41
 */
public class RequestExAddEnchantScrollItem extends L2GameClientPacket
{
	private int _objectId;
	private int _supportId;

	@Override
	protected void readImpl()
	{
		_supportId = readD();
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		L2ItemInstance scroll = activeChar.getInventory().getItemByObjectId(_supportId);
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(item == null || scroll == null)
		{
			return;
		}

		if(item.getOwnerId() != activeChar.getObjectId())
		{
			return;
		}

		// template for scroll
		EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);

		if(scrollTemplate == null || !scrollTemplate.isValid(item))
		{
			activeChar.sendPacket(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS);
			//activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new ExPutEnchantScrollItemResult(0));
			activeChar.setIsEnchanting(false);
			return;
		}

		activeChar.setActiveEnchantItem(scroll);
		activeChar.setIsEnchanting(true);
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantScrollItemResult(1));
	}

	@Override
	public String getType()
	{
		return "[C] D0:F0 RequestExAddEnchantScrollItem";
	}
}
