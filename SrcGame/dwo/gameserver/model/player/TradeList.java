package dwo.gameserver.model.player;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemRequest;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

/**
 * @author Advi
 */
public class TradeList
{
	private static final Logger _log = LogManager.getLogger(TradeList.class);

	private final L2PcInstance _owner;
	private final List<TradeItem> _items;
	private L2PcInstance _partner;
	private String _title;
	private boolean _packaged;

	private boolean _confirmed;
	private boolean _locked;

	public TradeList(L2PcInstance owner)
	{
		_items = new FastList<>();
		_owner = owner;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public L2PcInstance getPartner()
	{
		return _partner;
	}

	public void setPartner(L2PcInstance partner)
	{
		_partner = partner;
	}

	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public boolean isLocked()
	{
		return _locked;
	}

	public boolean isConfirmed()
	{
		return _confirmed;
	}

	public boolean isPackaged()
	{
		return _packaged;
	}

	public void setPackaged(boolean value)
	{
		_packaged = value;
	}

	/**
	 * Retrieves items from TradeList
	 */
	public TradeItem[] getItems()
	{
		return _items.toArray(new TradeItem[_items.size()]);
	}

	/**
	 * Returns the list of items in inventory available for transaction
	 *
	 * @return L2ItemInstance : items in inventory
	 */
	public TradeItem[] getAvailableItems(PcInventory inventory)
	{
		FastList<TradeItem> list = FastList.newInstance();
		for(TradeItem item : _items)
		{
			item = new TradeItem(item, item.getCount(), item.getPrice());
			inventory.adjustAvailableItem(item);
			list.add(item);
		}
		TradeItem[] result = list.toArray(new TradeItem[list.size()]);
		FastList.recycle(list);
		return result;
	}

	/**
	 * Returns Item List size
	 */
	public int getItemCount()
	{
		return _items.size();
	}

	/**
	 * Adjust available item from Inventory by the one in this list
	 *
	 * @param item : L2ItemInstance to be adjusted
	 * @return TradeItem representing adjusted item
	 */
	public TradeItem adjustAvailableItem(L2ItemInstance item)
	{
		if(item.isStackable())
		{
			for(TradeItem exclItem : _items)
			{
				if(exclItem.getItem().getItemId() == item.getItemId())
				{
					return item.getCount() <= exclItem.getCount() ? null : new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
				}
			}
		}
		return new TradeItem(item, item.getCount(), item.getReferencePrice());
	}

	/**
	 * Adjust ItemRequest by corresponding item in this list using its <b>ObjectId</b>
	 *
	 * @param item : ItemRequest to be adjusted
	 */
	public void adjustItemRequest(ItemRequest item)
	{
		for(TradeItem filtItem : _items)
		{
			if(filtItem.getObjectId() == item.getObjectId())
			{
				if(filtItem.getCount() < item.getCount())
				{
					item.setCount(filtItem.getCount());
				}
				return;
			}
		}
		item.setCount(0);
	}

	/**
	 * Add simplified item to TradeList
	 *
	 * @param objectId : int
	 * @param count    : int
	 * @return
	 */
	public TradeItem addItem(int objectId, long count)
	{
		synchronized(this)
		{
			return addItem(objectId, count, 0);
		}
	}

	/**
	 * Add item to TradeList
	 *
	 * @param objectId : int
	 * @param count    : long
	 * @param price    : long
	 * @return
	 */
	public TradeItem addItem(int objectId, long count, long price)
	{
		synchronized(this)
		{
			if(_locked)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to modify locked TradeList!");
				return null;
			}

			L2Object o = WorldManager.getInstance().findObject(objectId);
			if(!(o instanceof L2ItemInstance))
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to add invalid item to TradeList!");
				return null;
			}

			L2ItemInstance item = (L2ItemInstance) o;

			if(!(item.isTradeable() || _owner.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS) || item.isQuestItem())
			{
				return null;
			}

			if(!_owner.getInventory().canManipulateWithItemId(item.getItemId()))
			{
				return null;
			}

			if(count <= 0 || count > item.getCount())
			{
				return null;
			}

			if(!item.isStackable() && count > 1)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
				return null;
			}

			if(MAX_ADENA / count < price)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to overflow adena !");
				return null;
			}

			for(TradeItem checkitem : _items)
			{
				if(checkitem.getObjectId() == objectId)
				{
					return null;
				}
			}

			TradeItem titem = new TradeItem(item, count, price);
			_items.add(titem);

			// If Player has already confirmed this trade, invalidate the confirmation
			invalidateConfirmation();
			return titem;
		}
	}

	/**
	 * Add item to TradeList
	 *
	 * @param itemId   : int
	 * @param count    : long
	 * @param price    : long
	 * @return
	 */
	public TradeItem addItemByItemId(int itemId, long count, long price)
	{
		synchronized(this)
		{
			if(_locked)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to modify locked TradeList!");
				return null;
			}

			L2Item item = ItemTable.getInstance().getTemplate(itemId);
			if(item == null)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to add invalid item to TradeList!");
				return null;
			}

			if(!item.isTradeable() || item.isQuestItem())
			{
				return null;
			}

			if(!item.isStackable() && count > 1)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
				return null;
			}

			if(MAX_ADENA / count < price)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to overflow adena !");
				return null;
			}

			TradeItem titem = new TradeItem(item, count, price);
			_items.add(titem);

			// If Player has already confirmed this trade, invalidate the confirmation
			invalidateConfirmation();
			return titem;
		}
	}

	/**
	 * Remove item from TradeList
	 *
	 * @param objectId : int
	 * @param count    : int
	 * @return
	 */
	public TradeItem removeItem(int objectId, int itemId, long count)
	{
		synchronized(this)
		{
			if(_locked)
			{
				_log.log(Level.WARN, _owner.getName() + ": Attempt to modify locked TradeList!");
				return null;
			}

			for(TradeItem titem : _items)
			{
				if(titem.getObjectId() == objectId || titem.getItem().getItemId() == itemId)
				{
					// If Partner has already confirmed this trade, invalidate the confirmation
					if(_partner != null)
					{
						TradeList partnerList = _partner.getActiveTradeList();
						if(partnerList == null)
						{
							_log.log(Level.WARN, _partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
							return null;
						}
						partnerList.invalidateConfirmation();
					}

					// Reduce item count or complete item
					if(count != -1 && titem.getCount() > count)
					{
						titem.setCount(titem.getCount() - count);
					}
					else
					{
						_items.remove(titem);
					}

					return titem;
				}
			}
			return null;
		}
	}

	/**
	 * Update items in TradeList according their quantity in owner inventory
	 */
	public void updateItems()
	{
		synchronized(this)
		{
			for(TradeItem titem : _items)
			{
				L2ItemInstance item = _owner.getInventory().getItemByObjectId(titem.getObjectId());
				if(item == null || titem.getCount() < 1)
				{
					removeItem(titem.getObjectId(), -1, -1);
				}
				else if(item.getCount() < titem.getCount())
				{
					titem.setCount(item.getCount());
				}
			}
		}
	}

	/**
	 * Lockes TradeList, no further changes are allowed
	 */
	public void lock()
	{
		_locked = true;
	}

	/**
	 * Clears item list
	 */
	public void clear()
	{
		synchronized(this)
		{
			_items.clear();
			_locked = false;
		}
	}

	/**
	 * Confirms TradeList
	 *
	 * @return : boolean
	 */
	public boolean confirm()
	{
		if(_confirmed)
		{
			return true; // Already confirmed
		}

		// If Partner has already confirmed this trade, proceed exchange
		if(_partner != null)
		{
			TradeList partnerList = _partner.getActiveTradeList();
			if(partnerList == null)
			{
				_log.log(Level.WARN, _partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
				return false;
			}

			// Synchronization order to avoid deadlock
			TradeList sync1;
			TradeList sync2;
			if(_owner.getObjectId() > partnerList._owner.getObjectId())
			{
				sync1 = partnerList;
				sync2 = this;
			}
			else
			{
				sync1 = this;
				sync2 = partnerList;
			}

			synchronized(sync1)
			{
				synchronized(sync2)
				{
					_confirmed = true;
					if(partnerList._confirmed)
					{
						partnerList.lock();
						lock();
						if(!partnerList.validate())
						{
							return false;
						}
						if(!validate())
						{
							return false;
						}

						doExchange(partnerList);
					}
					else
					{
						_partner.onTradeConfirm(_owner);
					}
				}
			}
		}
		else
		{
			_confirmed = true;
		}

		return _confirmed;
	}

	/**
	 * Cancels TradeList confirmation
	 */
	public void invalidateConfirmation()
	{
		_confirmed = false;
	}

	/**
	 * Validates TradeList with owner inventory
	 */
	private boolean validate()
	{
		// Check for Owner validity
		if(_owner == null || WorldManager.getInstance().getPlayer(_owner.getObjectId()) == null)
		{
			_log.log(Level.WARN, "Invalid owner of TradeList");
			return false;
		}

		// Check for Item validity
		for(TradeItem titem : _items)
		{
			L2ItemInstance item = _owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), ProcessType.EXCHANGE);
			if(item == null || item.getCount() < 1)
			{
				_log.log(Level.WARN, _owner.getName() + ": Invalid Item in TradeList");
				return false;
			}
		}

		return true;
	}

	/**
	 * Transfers all TradeItems from inventory to partner
	 */
	private boolean TransferItems(L2PcInstance partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU)
	{
		for(TradeItem titem : _items)
		{
			L2ItemInstance oldItem = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if(oldItem == null)
			{
				return false;
			}
			L2ItemInstance newItem = _owner.getInventory().transferItem(ProcessType.TRADE, titem.getObjectId(), titem.getCount(), partner.getInventory(), _owner, _partner);
			if(newItem == null)
			{
				return false;
			}

			// Add changes to inventory update packets
			if(ownerIU != null)
			{
				if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
				{
					ownerIU.addModifiedItem(oldItem);
				}
				else
				{
					ownerIU.addRemovedItem(oldItem);
				}
			}

			if(partnerIU != null)
			{
				if(newItem.getCount() > titem.getCount())
				{
					partnerIU.addModifiedItem(newItem);
				}
				else
				{
					partnerIU.addNewItem(newItem);
				}
			}
		}
		return true;
	}

	/**
	 * Count items slots
	 */
	public int countItemsSlots(L2PcInstance partner)
	{
		int slots = 0;

		for(TradeItem item : _items)
		{
			if(item == null)
			{
				continue;
			}
			L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if(template == null)
			{
				continue;
			}
			if(!template.isStackable())
			{
				slots += item.getCount();
			}
			else if(partner.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
			{
				slots++;
			}
		}

		return slots;
	}

	/**
	 * Calc weight of items in tradeList
	 */
	public int calcItemsWeight()
	{
		long weight = 0;

		for(TradeItem item : _items)
		{
			if(item == null)
			{
				continue;
			}
			L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if(template == null)
			{
				continue;
			}
			weight += item.getCount() * template.getWeight();
		}

		return (int) Math.min(weight, Integer.MAX_VALUE);
	}

	/**
	 * Proceeds with trade
	 */
	private void doExchange(TradeList partnerList)
	{
		boolean success = false;

		// check weight and slots
		if(!_owner.getInventory().validateWeight(partnerList.calcItemsWeight()) || !partnerList._owner.getInventory().validateWeight(calcItemsWeight()))
		{
			partnerList._owner.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			_owner.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
		}
		else if(!_owner.getInventory().validateCapacity(partnerList.countItemsSlots(_owner)) || !partnerList._owner.getInventory().validateCapacity(countItemsSlots(partnerList._owner)))
		{
			partnerList._owner.sendPacket(SystemMessageId.SLOTS_FULL);
			_owner.sendPacket(SystemMessageId.SLOTS_FULL);
		}
		else
		{
			// Prepare inventory update packet
			InventoryUpdate ownerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
			InventoryUpdate partnerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

			// Transfer items
			partnerList.TransferItems(_owner, partnerIU, ownerIU);
			TransferItems(partnerList._owner, ownerIU, partnerIU);

			// Send inventory update packet
			if(ownerIU != null)
			{
				_owner.sendPacket(ownerIU);
			}
			else
			{
				_owner.sendPacket(new ItemList(_owner, false));
			}

			if(partnerIU != null)
			{
				_partner.sendPacket(partnerIU);
			}
			else
			{
				_partner.sendPacket(new ItemList(_partner, false));
			}

            _owner.sendPacket(new ExUserInfoInvenWeight(_owner));
            _owner.sendPacket(new ExAdenaInvenCount(_owner));
            _partner.sendPacket(new ExUserInfoInvenWeight(_partner));
            _partner.sendPacket(new ExAdenaInvenCount(_partner));

			success = true;
		}
		// Finish the trade
		partnerList._owner.onTradeFinish(success);
		_owner.onTradeFinish(success);
	}

	/**
	 * Buy items from this PrivateStore list
	 *
	 * @return int: result of trading. 0 - ok, 1 - canceled (no adena), 2 - failed (item error)
	 */
	public int privateStoreBuy(L2PcInstance player, FastSet<ItemRequest> items)
	{
		synchronized(this)
		{
			if(_locked)
			{
				return 1;
			}

			if(!validate())
			{
				lock();
				return 1;
			}

			if(!_owner.isOnline() || !player.isOnline())
			{
				return 1;
			}

			int slots = 0;
			int weight = 0;
			long totalPrice = 0;

			PcInventory ownerInventory = _owner.getInventory();
			PcInventory playerInventory = player.getInventory();

			for(ItemRequest item : items)
			{
				boolean found = false;

				for(TradeItem ti : _items)
				{
					if(ti.getObjectId() == item.getObjectId())
					{
						if(ti.getPrice() == item.getPrice())
						{
							if(ti.getCount() < item.getCount())
							{
								item.setCount(ti.getCount());
							}
							found = true;
						}
						break;
					}
				}
				// item with this objectId and price not found in tradelist
				if(!found)
				{
					if(_packaged)
					{
						Util.handleIllegalPlayerAction(player, "[TradeList.privateStoreBuy()] Player " + player.getName() + " tried to cheat the package sell and buy only a part of the package! Ban this player for bot usage!", Config.DEFAULT_PUNISH);
						return 2;
					}

					item.setCount(0);
					continue;
				}

				// check for overflow in the single item
				if(MAX_ADENA / item.getCount() < item.getPrice())
				{
					// private store attempting to overflow - disable it
					lock();
					return 1;
				}

				totalPrice += item.getCount() * item.getPrice();
				// check for overflow of the total price
				if(totalPrice > MAX_ADENA || totalPrice < 0)
				{
					// private store attempting to overflow - disable it
					lock();
					return 1;
				}

				// Check if requested item is available for manipulation
				L2ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), item.getCount(), ProcessType.SELL);
				if(oldItem == null || !oldItem.isTradeable())
				{
					// private store sell invalid item - disable it
					lock();
					return 2;
				}

				L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
				if(template == null)
				{
					continue;
				}
				weight += item.getCount() * template.getWeight();
				if(!template.isStackable())
				{
					slots += item.getCount();
				}
				else if(playerInventory.getItemByItemId(item.getItemId()) == null)
				{
					slots++;
				}
			}

			if(totalPrice > playerInventory.getAdenaCount())
			{
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return 1;
			}

			if(!playerInventory.validateWeight(weight))
			{
				player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
				return 1;
			}

			if(!playerInventory.validateCapacity(slots))
			{
				player.sendPacket(SystemMessageId.SLOTS_FULL);
				return 1;
			}

			// Prepare inventory update packets
			InventoryUpdate ownerIU = new InventoryUpdate();
			InventoryUpdate playerIU = new InventoryUpdate();

			L2ItemInstance adenaItem = playerInventory.getAdenaInstance();
			if(!playerInventory.reduceAdena(ProcessType.PRIVATESTORE, totalPrice, player, _owner))
			{
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return 1;
			}
			playerIU.addItem(adenaItem);
			ownerInventory.addAdena(ProcessType.PRIVATESTORE, totalPrice, _owner, player);
			//ownerIU.addItem(ownerInventory.getAdenaInstance());

			boolean ok = true;

			// Transfer items
			for(ItemRequest item : items)
			{
				if(item.getCount() == 0)
				{
					continue;
				}

				// Check if requested item is available for manipulation
				L2ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), item.getCount(), ProcessType.SELL);
				if(oldItem == null)
				{
					// should not happens - validation already done
					lock();
					ok = false;
					break;
				}

				// Proceed with item transfer
				L2ItemInstance newItem = ownerInventory.transferItem(ProcessType.PRIVATESTORE, item.getObjectId(), item.getCount(), playerInventory, _owner, player);
				if(newItem == null)
				{
					ok = false;
					break;
				}
				removeItem(item.getObjectId(), -1, item.getCount());

				// Add changes to inventory update packets
				if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
				{
					ownerIU.addModifiedItem(oldItem);
				}
				else
				{
					ownerIU.addRemovedItem(oldItem);
				}
				if(newItem.getCount() > item.getCount())
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}

				// Send messages about the transaction to both players
				if(newItem.isStackable())
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S3_S2_S);
					msg.addString(player.getName());
					msg.addItemName(newItem);
					msg.addItemNumber(item.getCount());
					_owner.sendPacket(msg);

					msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_C1);
					msg.addString(_owner.getName());
					msg.addItemName(newItem);
					msg.addItemNumber(item.getCount());
					player.sendPacket(msg);
				}
				else
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S2);
					msg.addString(player.getName());
					msg.addItemName(newItem);
					_owner.sendPacket(msg);

					msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_C1);
					msg.addString(_owner.getName());
					msg.addItemName(newItem);
					player.sendPacket(msg);
				}
			}

			// Send inventory update packet
			_owner.sendPacket(ownerIU);
			player.sendPacket(playerIU);
			return ok ? 0 : 2;
		}
	}

	/**
	 * Sell items to this PrivateStore list
	 *
	 * @return : boolean true if success
	 */
	public boolean privateStoreSell(L2PcInstance player, ItemRequest[] items)
	{
		synchronized(this)
		{
			if(_locked)
			{
				return false;
			}

			if(!_owner.isOnline() || !player.isOnline())
			{
				return false;
			}

			boolean ok = false;

			PcInventory ownerInventory = _owner.getInventory();
			PcInventory playerInventory = player.getInventory();

			// Prepare inventory update packet
			InventoryUpdate ownerIU = new InventoryUpdate();
			InventoryUpdate playerIU = new InventoryUpdate();

			long totalPrice = 0;

			for(ItemRequest item : items)
			{
				// searching item in tradelist using itemId
				boolean found = false;

				for(TradeItem ti : _items)
				{
					if(ti.getItem().getItemId() == item.getItemId())
					{
						// price should be the same
						if(ti.getPrice() == item.getPrice())
						{
							// if requesting more than available - decrease count
							if(ti.getCount() < item.getCount())
							{
								item.setCount(ti.getCount());
							}
							found = item.getCount() > 0;
						}
						break;
					}
				}
				// not found any item in the tradelist with same itemId and price
				// maybe another player already sold this item ?
				if(!found)
				{
					continue;
				}

				// check for overflow in the single item
				if(MAX_ADENA / item.getCount() < item.getPrice())
				{
					lock();
					break;
				}

				long _totalPrice = totalPrice + item.getCount() * item.getPrice();
				// check for overflow of the total price
				if(_totalPrice > MAX_ADENA || _totalPrice < 0)
				{
					lock();
					break;
				}

				if(ownerInventory.getAdenaCount() < _totalPrice)
				{
					continue;
				}

				// Check if requested item is available for manipulation
				int objectId = item.getObjectId();
				L2ItemInstance oldItem = player.checkItemManipulation(objectId, item.getCount(), ProcessType.SELL);
				// private store - buy use same objectId for buying several non-stackable items
				if(oldItem == null)
				{
					// searching other items using same itemId
					oldItem = playerInventory.getItemByItemId(item.getItemId());
					if(oldItem == null)
					{
						continue;
					}
					objectId = oldItem.getObjectId();
					oldItem = player.checkItemManipulation(objectId, item.getCount(), ProcessType.SELL);
					if(oldItem == null)
					{
						continue;
					}
				}

				if(!oldItem.isTradeable())
				{
					continue;
				}

				// Proceed with item transfer
				L2ItemInstance newItem = playerInventory.transferItem(ProcessType.PRIVATESTORE, objectId, item.getCount(), ownerInventory, player, _owner);
				if(newItem == null)
				{
					continue;
				}

				removeItem(-1, item.getItemId(), item.getCount());
				ok = true;

				// increase total price only after successful transaction
				totalPrice = _totalPrice;

				// Add changes to inventory update packets
				if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
				if(newItem.getCount() > item.getCount())
				{
					ownerIU.addModifiedItem(newItem);
				}
				else
				{
					ownerIU.addNewItem(newItem);
				}

				// Send messages about the transaction to both players
				if(newItem.isStackable())
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_C1);
					msg.addString(player.getName());
					msg.addItemName(newItem);
					msg.addItemNumber(item.getCount());
					_owner.sendPacket(msg);

					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S3_S2_S);
					msg.addString(_owner.getName());
					msg.addItemName(newItem);
					msg.addItemNumber(item.getCount());
					player.sendPacket(msg);
				}
				else
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_C1);
					msg.addString(player.getName());
					msg.addItemName(newItem);
					_owner.sendPacket(msg);

					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S2);
					msg.addString(_owner.getName());
					msg.addItemName(newItem);
					player.sendPacket(msg);
				}
			}

			if(totalPrice > 0)
			{
				// Transfer adena
				if(totalPrice > ownerInventory.getAdenaCount())
				{
					return false;                // should not happens, just a precaution
				}

				L2ItemInstance adenaItem = ownerInventory.getAdenaInstance();
				ownerInventory.reduceAdena(ProcessType.PRIVATESTORE, totalPrice, _owner, player);
				ownerIU.addItem(adenaItem);
				playerInventory.addAdena(ProcessType.PRIVATESTORE, totalPrice, player, _owner);
				playerIU.addItem(playerInventory.getAdenaInstance());
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					player.updateWorldStatistic(CategoryType.PRIVATE_SELL_COUNT, null, 1);
				}
			}

			if(ok)
			{
				// Send inventory update packet
				_owner.sendPacket(ownerIU);
				player.sendPacket(playerIU);
			}
			return ok;
		}
	}
}