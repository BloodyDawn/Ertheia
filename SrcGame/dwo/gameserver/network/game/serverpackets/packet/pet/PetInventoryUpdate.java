package dwo.gameserver.network.game.serverpackets.packet.pet;

import dwo.config.Config;
import dwo.gameserver.model.items.ItemInfo;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.AbstractInventoryUpdate;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;
import java.util.stream.Collectors;

public class PetInventoryUpdate extends AbstractInventoryUpdate
{
	private List<ItemInfo> _items;

	public PetInventoryUpdate()
	{
	}

    public PetInventoryUpdate(L2ItemInstance item)
    {
        super(item);
    }

    public PetInventoryUpdate(List<ItemInfo> items)
    {
        super(items);
    }


	private void showDebug()
	{
		for(ItemInfo item : _items)
		{
			_log.log(Level.DEBUG, "oid:" + Integer.toHexString(item.getObjectId()) +
				" item:" + item.getItem().getName() + " last change:" + item.getChange());
		}
	}

	@Override
	protected void writeImpl()
	{
		writeItems();
	}
}
