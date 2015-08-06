package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.player.jump.L2JumpType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 19.06.2011
 * Time: 15:58:17
 */

public class ExFlyMoveBroadcast extends L2GameServerPacket
{
	private int _objectId;
	private L2JumpType _type;
	private int _DestX;
	private int _DestY;
	private int _DestZ;
	private int _x;
	private int _y;
	private int _z;
	private int _size;
	private int _id;

	public ExFlyMoveBroadcast(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}

	public ExFlyMoveBroadcast(L2Character obj, L2JumpType type, int x, int y, int z, int size, int id)
	{
		_objectId = obj.getObjectId();
		_type = type;
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_DestX = x;
		_DestY = y;
		_DestZ = z;
		_size = size;
		_id = id;
	}

	@Override
	protected void writeImpl()
	{
		// ddddd ddddd
		writeD(_objectId);
		writeD(_type.ordinal());

		writeD(0x00);    //?

		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeD(0x00);   //?

		writeD(_DestX);
		writeD(_DestY);
		writeD(_DestZ);
	}
}
