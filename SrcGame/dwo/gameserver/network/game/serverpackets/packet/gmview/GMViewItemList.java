package dwo.gameserver.network.game.serverpackets.packet.gmview;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class GMViewItemList extends L2GameServerPacket
{
	private L2ItemInstance[] _items;
	private int _limit;
	private String _playerName;

	public GMViewItemList(L2PcInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}

	public GMViewItemList(L2PetInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}

	@Override
	protected void writeImpl()
	{
		writeS(_playerName);
		writeD(_limit); // inventory limit
		writeH(0x01); // show window ??
		writeH(_items.length);

		for(L2ItemInstance temp : _items)
		{
			writeItemInfo(temp);
		}
	}
}
