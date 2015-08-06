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

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.items.itemcontainer.PcFreight;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

/**
 * @author -Wooden-
 * @author UnAfraid
 * Thanks mrTJO
 */
public class RequestPackageSend extends L2GameClientPacket
{
	private ItemHolder[] _items;
	private int _objectId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		_items = new ItemHolder[_count];
		for(int i = 0; i < _count; i++)
		{
			int objId = readD();
			long cnt = readQ();
			if(objId < 1 || cnt < 0)
			{
				_items = null;
				return;
			}

			_items[i] = new ItemHolder(objId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		if(_items == null)
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.DEPOSIT_ITEM))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}

		player.setActiveWarehouse(new PcFreight(_objectId));

		ItemContainer warehouse = player.getActiveWarehouse();
		if(warehouse == null)
		{
			return;
		}

		L2Npc manager = player.getLastFolkNPC();
		if((manager == null || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
		{
			return;
		}

		if(player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
			return;
		}

		// Alt game - Reputation punishment
		if(player.hasBadReputation())
		{
			return;
		}

		// Freight price from config or normal price per item slot (30)
		int fee = _count * Config.ALT_FREIGHT_PRIECE; //Config.ALT_GAME_FREIGHT_PRICE;
		double currentAdena = player.getAdenaCount();
		int slots = 0;

		for(ItemHolder i : _items)
		{
			// Check validity of requested item
			L2ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), ProcessType.DEPOSIT);
			if(item == null)
			{
				_log.log(Level.WARN, "Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				return;
			}

			if(!item.isFreightable())
			{
				return;
			}

			// Calculate needed adena and slots
			if(item.getItemId() == PcInventory.ADENA_ID)
			{
				currentAdena -= i.getCount();
			}
			else if(!item.isStackable())
			{
				slots += i.getCount();
			}
			else if(warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		// Item Max Limit Check
		if(!warehouse.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		// Check if enough adena and charge the fee
		if(currentAdena < fee || !player.reduceAdena(ProcessType.WAREHOUSE, fee, manager, false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		// get current tradelist if any
		if(player.getActiveTradeList() != null)
		{
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for(ItemHolder i : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), ProcessType.DEPOSIT);
			if(oldItem == null)
			{
				_log.log(Level.WARN, "Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				return;
			}

			L2ItemInstance newItem = player.getInventory().transferItem(ProcessType.TRADE, i.getId(), i.getCount(), warehouse, player, null);
			if(newItem == null)
			{
				_log.log(Level.WARN, "Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}

			if(playerIU != null)
			{
				if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}

		warehouse.deleteMe();

		// Send updated item list to the player
		if(playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}

		// Обновляем вес инвентаря
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	@Override
	public String getType()
	{
		return "[C] 9F RequestPackageSend";
	}
}