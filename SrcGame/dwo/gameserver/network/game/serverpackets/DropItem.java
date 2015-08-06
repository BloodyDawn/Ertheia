package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;

public class DropItem extends L2GameServerPacket
{
	private L2ItemInstance _item;
	private int _charObjId;

	/**
	 * Constructor of the DropItem server packet
	 * @param item : L2ItemInstance designating the item
	 * @param playerObjId : int designating the player ID who dropped the item
	 */
	public DropItem(L2ItemInstance item, int playerObjId)
	{
		_item = item;
		_charObjId = playerObjId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());

		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());

		writeC(_item.isStackable() ? 0x01 : 0x00);
		writeQ(_item.getCount());

		writeC(0x00);
	}
}
