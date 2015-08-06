package dwo.gameserver.network.game.serverpackets.packet.vehicle.boat;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

/**
 * @author Maktakien
 */

public class GetOnVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatObjId;
	private Point3D _pos;

	public GetOnVehicle(int charObjId, int boatObjId, Point3D pos)
	{
		_charObjId = charObjId;
		_boatObjId = boatObjId;
		_pos = pos;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
	}
}
