package dwo.gameserver.network.game.serverpackets.packet.enchant.item;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ChooseInventoryItem extends L2GameServerPacket
{

	private int _itemId;

	public ChooseInventoryItem(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemId);
	}
}
