package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 14.10.12
 * Time: 1:49
 * L2GOD Team/
 */
public class ExTeleportToLocationActivate extends L2GameServerPacket
{
	private int _targetObjId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	public ExTeleportToLocationActivate(L2Object obj, int x, int y, int z, int heading)
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
		writeC(0x54); // ??
		writeC(0x01); // ??
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00); // isValidation ??
		writeD(_heading); // nYaw
		writeD(0x00); // ??
	}
}
