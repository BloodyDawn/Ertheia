/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.clientpackets.packet.enchant.item;

import dwo.gameserver.datatables.xml.EnchantItemData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.EnchantScroll;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.enchant.item.ExPutEnchantTargetItemResult;

public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(_objectId == 0 || activeChar == null)
		{
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();

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
			// activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.setIsEnchanting(false);
			return;
		}

		activeChar.setIsEnchanting(true);
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantTargetItemResult(1));
	}

	@Override
	public String getType()
	{
		return "[C] D0:4F RequestExTryToPutEnchantTargetItem";
	}
}
