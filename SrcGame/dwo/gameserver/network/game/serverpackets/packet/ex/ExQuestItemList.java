package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

/**
 * @author JIV
 */

public class ExQuestItemList extends L2GameServerPacket
{
	private FastList<L2ItemInstance> _items;
	private PcInventory _inventory;

	public ExQuestItemList(FastList<L2ItemInstance> items, PcInventory inv)
	{
		_items = items;
		_inventory = inv;
	}

	@Override
	protected void writeImpl()
	{
		writeH(_items.size());
		_items.forEach(this::writeItemInfo);
		if(_inventory.hasInventoryBlock())
		{
			writeH(_inventory.getBlockItems().length);
			writeC(_inventory.getBlockMode());
			for(int i : _inventory.getBlockItems())
			{
				writeD(i);
			}
		}
		else
		{
			writeH(0x00);
		}
		FastList.recycle(_items);
	}
}
