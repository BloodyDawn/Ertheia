package dwo.gameserver.network.game.serverpackets.packet.vehicle.boat;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ValidateLocation extends L2GameServerPacket
{
	private int _charObjId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	public ValidateLocation(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
