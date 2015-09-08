package dwo.gameserver.network.game.serverpackets;

import dwo.config.Config;
import dwo.gameserver.model.items.ItemInfo;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

public class InventoryUpdate extends AbstractInventoryUpdate
{
	private List<ItemInfo> _items;

	public InventoryUpdate()
	{
		_items = new FastList<>();
		if(Config.DEBUG)
		{
			showDebug();
		}
	}
	public InventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
		if(Config.DEBUG)
		{
			showDebug();
		}
	}

	private void showDebug()
	{
		for(ItemInfo item : _items)
		{
			_log.log(Level.DEBUG, "oid:" + Integer.toHexString(item.getObjectId()) + " item:" + item.getItem().getName() + " last change:" + item.getChange());
		}
	}

	@Override
	protected void writeImpl()
	{
		writeItems();
	}
}
