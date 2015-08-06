package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;

public class MTL extends L2GameServerPacket
{
	private int _charObjId;
	private int _x;
	private int _y;
	private int _z;
	private int _dx;
	private int _dy;
	private int _dz;

	public MTL(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_dx = cha.getXdestination();
		_dy = cha.getYdestination();
		_dz = cha.getZdestination();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_dx);
		writeD(_dy);
		writeD(_dz);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
