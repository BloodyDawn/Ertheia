package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;

public class StopMove extends L2GameServerPacket
{
	private int _objectId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	public StopMove(L2Character cha)
	{
		this(cha.getObjectId(), cha.getX(), cha.getY(), cha.getZ(), cha.getHeading());
	}

	public StopMove(int objectId, int x, int y, int z, int heading)
	{
		_objectId = objectId;
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
