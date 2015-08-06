package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

public class ItemsOnGroundAutoDestroyManager
{
	protected static final Logger _log = LogManager.getLogger(ItemsOnGroundAutoDestroyManager.class);
	protected static long _sleep;
	protected List<L2ItemInstance> _items;

	private ItemsOnGroundAutoDestroyManager()
	{
		_log.log(Level.INFO, "ItemsAutoDestroy : Initializing...");
		_items = new FastList<>();
		_sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
		if(_sleep == 0) // it should not happend as it is not called when AUTODESTROY_ITEM_AFTER = 0 but we never know..
		{
			_sleep = 3600000;
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 5000, 5000);
	}

	public static ItemsOnGroundAutoDestroyManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addItem(L2ItemInstance item)
	{
		synchronized(this)
		{
			item.setDropTime(System.currentTimeMillis());
			_items.add(item);
		}
	}

	public void removeItems()
	{
		synchronized(this)
		{
			if(_items.isEmpty())
			{
				return;
			}

			long curtime = System.currentTimeMillis();
			for(L2ItemInstance item : _items)
			{
				if(item == null || item.getDropTime() == 0 || item.getItemLocation() != L2ItemInstance.ItemLocation.VOID)
				{
					_items.remove(item);
				}
				else
				{
					if(item.getItem().getAutoDestroyTime() > 0)
					{
						if(curtime - item.getDropTime() > item.getItem().getAutoDestroyTime())
						{
							WorldManager.getInstance().removeVisibleObject(item, item.getLocationController().getWorldRegion());
							WorldManager.getInstance().removeObject(item);
							_items.remove(item);
							if(Config.SAVE_DROPPED_ITEM)
							{
								ItemsOnGroundManager.getInstance().removeObject(item);
							}
						}
					}
					else if(item.getItemType() == L2EtcItemType.HERB)
					{
						if(curtime - item.getDropTime() > Config.HERB_AUTO_DESTROY_TIME)
						{
							WorldManager.getInstance().removeVisibleObject(item, item.getLocationController().getWorldRegion());
							WorldManager.getInstance().removeObject(item);
							_items.remove(item);
							if(Config.SAVE_DROPPED_ITEM)
							{
								ItemsOnGroundManager.getInstance().removeObject(item);
							}
						}
					}
					else if(curtime - item.getDropTime() > _sleep)
					{
						WorldManager.getInstance().removeVisibleObject(item, item.getLocationController().getWorldRegion());
						WorldManager.getInstance().removeObject(item);
						_items.remove(item);
						if(Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final ItemsOnGroundAutoDestroyManager _instance = new ItemsOnGroundAutoDestroyManager();
	}

	protected class CheckItemsForDestroy extends Thread
	{
		@Override
		public void run()
		{
			removeItems();
		}
	}
}