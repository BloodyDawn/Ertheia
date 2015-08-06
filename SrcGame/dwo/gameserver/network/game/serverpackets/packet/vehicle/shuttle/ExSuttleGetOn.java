package dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.07.11
 * Time: 15:20
 */

public class ExSuttleGetOn extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _shuttleObjId;
	private final Point3D _pos;

	public ExSuttleGetOn(int charObjId, int shuttleObjId, Point3D pos)
	{
		_charObjId = charObjId;
		_shuttleObjId = shuttleObjId;
		_pos = pos;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_shuttleObjId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
	}
}
