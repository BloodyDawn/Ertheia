package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Object;

public class TeleportToLocation extends L2GameServerPacket
{
	private int _targetObjId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	public TeleportToLocation(L2Object obj, int x, int y, int z, int heading)
	{
		_targetObjId = obj.getObjectId();
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00); // isValidation ??
		writeD(_heading); // nYaw
	}
}
