package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;

public class MoveToPawn extends L2GameServerPacket
{
	private int _charObjId;
	private int _targetId;
	private int _distance;
	private int _x;
	private int _y;
	private int _z;
	private int _tx;
	private int _ty;
	private int _tz;

	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}
}
