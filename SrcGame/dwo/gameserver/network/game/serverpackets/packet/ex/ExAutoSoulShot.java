package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExAutoSoulShot extends L2GameServerPacket
{
	private int _itemId;
	private int _type;

	public ExAutoSoulShot(int itemId, int type)
	{
		_itemId = itemId;
		_type = type;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemId);
		writeD(_type);
	}
}
