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
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.itemcontainer.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Format:(ch) d[dd]
 *
 * @author -Wooden-
 */
public class RequestSaveInventoryOrder extends L2GameClientPacket
{
	/** client limit */
	private static final int LIMIT = 125;
	private List<InventoryOrder> _order;

	@Override
	protected void readImpl()
	{
		int sz = readD();
		sz = Math.min(sz, LIMIT);
		_order = new ArrayList<>(sz);
		for(int i = 0; i < sz; i++)
		{
			int objectId = readD();
			int order = readD();
			_order.add(new InventoryOrder(objectId, order));
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player != null)
		{
			Inventory inventory = player.getInventory();
			for(InventoryOrder order : _order)
			{
				L2ItemInstance item = inventory.getItemByObjectId(order.objectID);
				if(item != null && item.getItemLocation() == ItemLocation.INVENTORY)
				{
					item.setLocation(ItemLocation.INVENTORY, order.order);
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:49 RequestSaveInventoryOrder";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}

	private static class InventoryOrder
	{
		int order;

		int objectID;

		public InventoryOrder(int id, int ord)
		{
			objectID = id;
			order = ord;
		}
	}
}
