package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;

public class SpawnItem extends L2GameServerPacket
{
	private int _objectId;
	private int _itemId;
	private int _x;
	private int _y;
	private int _z;
	private int _stackable;
	private long _count;

	public SpawnItem(L2Object obj)
	{
		_objectId = obj.getObjectId();
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();

		if(obj instanceof L2ItemInstance)
		{
			L2ItemInstance item = (L2ItemInstance) obj;
			_itemId = item.getItemId();
			_stackable = item.isStackable() ? 0x01 : 0x00;
			_count = item.getCount();
		}
		else
		{
			_itemId = obj.getPolyController().getPolyId();
			_stackable = 0;
			_count = 1;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_itemId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		// only show item count if it is a stackable item
		writeD(_stackable);
		writeQ(_count);
		writeD(0x00); // c2
		writeD(0x00); // freya unk
	}
}
