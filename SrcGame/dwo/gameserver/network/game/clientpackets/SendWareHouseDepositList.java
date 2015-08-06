package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.items.itemcontainer.PcWarehouse;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;

public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	private ItemHolder[] _items;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new ItemHolder[count];
		for(int i = 0; i < count; i++)
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

		ItemContainer warehouse = player.getActiveWarehouse();
		if(warehouse == null)
		{
			return;
		}

		boolean isPrivate = warehouse instanceof PcWarehouse;

		L2Npc manager = player.getLastFolkNPC();
		if((manager == null || !manager.canInteract(player)) && !player.isGM())
		{
			return;
		}

		if(!isPrivate && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
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
		long fee = _items.length * 30;
		long currentAdena = player.getAdenaCount();
		int slots = 0;

		for(ItemHolder warehouseItem : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(warehouseItem.getId(), warehouseItem.getCount(), ProcessType.DEPOSIT);
			if(item == null)
			{
				_log.log(Level.WARN, "Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				return;
			}

			// Calculate needed adena and slots
			if(item.getItemId() == ADENA_ID)
			{
				currentAdena -= warehouseItem.getCount();
			}
			if(!item.isStackable())
			{
				slots += warehouseItem.getCount();
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
		for(ItemHolder warehouseItem : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(warehouseItem.getId(), warehouseItem.getCount(), ProcessType.DEPOSIT);
			if(oldItem == null)
			{
				_log.log(Level.WARN, "Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				return;
			}

			if(!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate))
			{
				continue;
			}

			L2ItemInstance newItem = player.getInventory().transferItem(ProcessType.WAREHOUSE, warehouseItem.getId(), warehouseItem.getCount(), warehouse, player, manager);
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

			// Обновление статистики Адены клана
			int clanId = -1;
			if(newItem.getItemLocation() == L2ItemInstance.ItemLocation.CLANWH && oldItem.getItemId() == ADENA_ID)
			{
				clanId = player.getClanId();
				if(clanId > 0 && newItem.getCount() > 0)
				{
					WorldStatisticsManager.getInstance().updateClanStat(clanId, CategoryType.ADENA_COUNT_IN_WH, 0, newItem.getCount());
				}
			}
		}

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
		return "[C] 31 SendWareHouseDepositList";
	}
}