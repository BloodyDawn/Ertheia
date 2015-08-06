package dwo.gameserver.network.game.serverpackets;

import dwo.config.Config;
import dwo.gameserver.model.items.ItemInfo;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;
import java.util.stream.Collectors;

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

	/**
	 * @param items
	 */
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

    @Override
    public void runImpl()
    {
        getClient().getActiveChar().sendPacket(new ExUserInfoInvenWeight(getClient().getActiveChar()));
    }
}
