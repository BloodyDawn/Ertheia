package dwo.gameserver.network.game.serverpackets.packet.attribute;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private L2ItemInstance[] _items;
	private long _price;

	public ExShowBaseAttributeCancelWindow(L2PcInstance player)
	{
		_items = player.getInventory().getElementItems();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_items.length);
		for(L2ItemInstance item : _items)
		{
			writeD(item.getObjectId());
			writeQ(getPrice(item));
		}
	}

	private long getPrice(L2ItemInstance item)
	{
		switch(item.getItem().getCrystalType())
		{
			case S:
				_price = item.getItem() instanceof L2Weapon ? 50000 : 40000;
				break;
			case S80:
				_price = item.getItem() instanceof L2Weapon ? 100000 : 80000;
				break;
			case S84:
				_price = item.getItem() instanceof L2Weapon ? 200000 : 160000;
				break;
			case R:
				_price = item.getItem() instanceof L2Weapon ? 250000 : 240000;
				break;
			case R95:
				_price = item.getItem() instanceof L2Weapon ? 300000 : 280000;
				break;
			case R99:
				_price = item.getItem() instanceof L2Weapon ? 350000 : 320000;
				break;
		}

		return _price;
	}
}