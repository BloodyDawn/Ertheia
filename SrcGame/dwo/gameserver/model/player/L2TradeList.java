package dwo.gameserver.model.player;

import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import javolution.util.FastMap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class L2TradeList
{
	private FastMap<Integer, L2TradeItem> _items;
	private int _shopId;
	private String _buystorename;
	private String _sellstorename;
	private boolean _hasLimitedStockItem;
	private String _npcId;

	public L2TradeList(int shopId)
	{
		_items = new FastMap<Integer, L2TradeItem>().shared();
		_shopId = shopId;
	}

	public String getNpcId()
	{
		return _npcId;
	}

	public void setNpcId(String id)
	{
		_npcId = id;
	}

	public void addItem(L2TradeItem item)
	{
		_items.put(item.getItemId(), item);
		if(item.hasLimitedStock())
		{
			_hasLimitedStockItem = true;
		}
	}

	public void replaceItem(int itemID, long price)
	{
		L2TradeItem item = _items.get(itemID);
		if(item != null)
		{
			item.setPrice(price);
		}
	}

	public void removeItem(int itemID)
	{
		_items.remove(itemID);
	}

	/**
	 * @return ShopID у NPC.
	 */
	public int getShopId()
	{
		return _shopId;
	}

	/**
	 * @param hasLimitedStockItem The hasLimitedStockItem to set.
	 */
	public void setHasLimitedStockItem(boolean hasLimitedStockItem)
	{
		_hasLimitedStockItem = hasLimitedStockItem;
	}

	/**
	 * @return the hasLimitedStockItem.
	 */
	public boolean hasLimitedStockItem()
	{
		return _hasLimitedStockItem;
	}

	/**
	 * @return the items.
	 */
	public Collection<L2TradeItem> getItems()
	{
		return _items.values();
	}

	public List<L2TradeItem> getItems(int start, int end)
	{
		List<L2TradeItem> list = new LinkedList<>();
		list.addAll(_items.values());
		return list.subList(start, end);
	}

	public long getPriceForItemId(int itemId)
	{
		L2TradeItem item = _items.get(itemId);
		if(item != null)
		{
			return item.getPrice();
		}
		return -1;
	}

	public L2TradeItem getItemById(int itemId)
	{
		return _items.get(itemId);
	}

	public boolean containsItemId(int itemId)
	{
		return _items.containsKey(itemId);
	}

	/**
	 * Items representation for trade lists
	 *
	 * @author KenM
	 */
	public static class L2TradeItem
	{
		private final int _itemId;
		private final L2Item _template;
		private long _price;

		// count related
		private AtomicLong _currentCount = new AtomicLong();
		private long _maxCount = -1;
		private long _restoreDelay;
		private long _nextRestoreTime;

		public L2TradeItem(int itemId)
		{
			_itemId = itemId;
			_template = ItemTable.getInstance().getTemplate(itemId);
		}

		/**
		 * @return the itemId.
		 */
		public int getItemId()
		{
			return _itemId;
		}

		/**
		 * @return the price.
		 */
		public long getPrice()
		{
			return _price;
		}

		/**
		 * @param price The price to set.
		 */
		public void setPrice(long price)
		{
			_price = price;
		}

		public L2Item getTemplate()
		{
			return _template;
		}

		public boolean decreaseCount(long val)
		{
			return _currentCount.addAndGet(-val) >= 0;
		}

		/**
		 * @return the currentCount.
		 */
		public long getCurrentCount()
		{
			if(hasLimitedStock() && isPendingStockUpdate())
			{
				restoreInitialCount();
			}
			long ret = _currentCount.get();
			return ret > 0 ? ret : 0;
		}

		/**
		 * @param currentCount The currentCount to set.
		 */
		public void setCurrentCount(long currentCount)
		{
			_currentCount.set(currentCount);
		}

		public boolean isPendingStockUpdate()
		{
			return System.currentTimeMillis() >= _nextRestoreTime;
		}

		public void restoreInitialCount()
		{
			setCurrentCount(_maxCount);
			_nextRestoreTime += _restoreDelay;

			// consume until next update is on future
			if(isPendingStockUpdate() && _restoreDelay > 0)
			{
				_nextRestoreTime = System.currentTimeMillis() + _restoreDelay;
			}
		}

		/**
		 * @return the maxCount.
		 */
		public long getMaxCount()
		{
			return _maxCount;
		}

		/**
		 * @param maxCount The maxCount to set.
		 */
		public void setMaxCount(long maxCount)
		{
			_maxCount = maxCount;
		}

		public boolean hasLimitedStock()
		{
			return _maxCount > -1;
		}

		/**
		 * @return the restoreDelay (in milis)
		 */
		public long getRestoreDelay()
		{
			return _restoreDelay;
		}

		/**
		 * @param restoreDelay The restoreDelay to set (in hours)
		 */
		public void setRestoreDelay(long restoreDelay)
		{
			_restoreDelay = restoreDelay * 60 * 60 * 1000;
		}

		/**
		 * @return the nextRestoreTime.
		 */
		public long getNextRestoreTime()
		{
			return _nextRestoreTime;
		}

		/**
		 * For resuming when server loads
		 *
		 * @param nextRestoreTime The nextRestoreTime to set.
		 */
		public void setNextRestoreTime(long nextRestoreTime)
		{
			_nextRestoreTime = nextRestoreTime;
		}
	}
}
