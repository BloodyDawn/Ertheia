package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;

public class GetItem extends L2GameServerPacket
{
	private L2ItemInstance _item;
	private int _playerId;

	public GetItem(L2ItemInstance item, int playerId)
	{
		_item = item;
		_playerId = playerId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_playerId);
		writeD(_item.getObjectId());

		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());
	}
}
