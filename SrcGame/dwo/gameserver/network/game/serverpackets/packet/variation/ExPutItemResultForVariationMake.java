package dwo.gameserver.network.game.serverpackets.packet.variation;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExPutItemResultForVariationMake extends L2GameServerPacket
{
	private int _itemObjId;
	private int _itemId;

	public ExPutItemResultForVariationMake(int itemObjId, int itemId)
	{
		_itemObjId = itemObjId;
		_itemId = itemId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(1);
	}
}
