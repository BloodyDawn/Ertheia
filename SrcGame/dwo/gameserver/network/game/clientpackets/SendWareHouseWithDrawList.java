package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.ClanWarehouse;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.items.itemcontainer.PcWarehouse;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import org.apache.log4j.Level;

public class SendWareHouseWithDrawList extends L2GameClientPacket
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

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.WITHDRAW_ITEM))
		{
			player.sendMessage("You withdrawing items too fast.");
			return;
		}

		ItemContainer warehouse = player.getActiveWarehouse();
		if(warehouse == null)
		{
			return;
		}

		L2Npc manager = player.getLastFolkNPC();
		if((manager == null || !manager.canInteract(player)) && !player.isGM())
		{
			return;
		}

		if(!(warehouse instanceof PcWarehouse) && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			return;
		}

		// Alt game - Reputation punishment
		if(player.hasBadReputation())
		{
			return;
		}

		if(Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
		{
			if(warehouse instanceof ClanWarehouse && (player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
			{
				return;
			}
		}
		else
		{
			if(warehouse instanceof ClanWarehouse && !player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				return;
			}
		}

		int weight = 0;
		int slots = 0;

		for(ItemHolder i : _items)
		{
			// Calculate needed slots
			L2ItemInstance item = warehouse.getItemByObjectId(i.getId());
			if(item == null || item.getCount() < i.getCount())
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character "+ player.getName() + " of account "+ player.getAccountName() + " tried to withdraw non-existent item from warehouse.", Config.DEFAULT_PUNISH);
				return;
			}

			weight += i.getCount() * item.getItem().getWeight();
			if(!item.isStackable())
			{
				slots += i.getCount();
			}
			else if(player.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		// Item Max Limit Check
		if(!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		// Weight limit Check
		if(!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for(ItemHolder i : _items)
		{
			L2ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
			if(oldItem == null || oldItem.getCount() < i.getCount())
			{
				_log.log(Level.WARN, "Error withdrawing a warehouse object for char " + player.getName() + " (olditem == null)");
				return;
			}
			L2ItemInstance newItem = warehouse.transferItem(ProcessType.WAREHOUSE, i.getId(), i.getCount(), player.getInventory(), player, manager);
			if(newItem == null)
			{
				_log.log(Level.WARN, "Error withdrawing a warehouse object for char " + player.getName() + " (newitem == null)");
				return;
			}

			if(playerIU != null)
			{
				if(newItem.getCount() > i.getCount())
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
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

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	@Override
	public String getType()
	{
		return "[C] 32 SendWareHouseWithDrawList";
	}
}