package dwo.gameserver.network.game.serverpackets.packet.vehicle.boat;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatObjId;
	private int _heading;
	private Point3D _pos;

	public ValidateLocationInVehicle(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_boatObjId = player.getBoat().getObjectId();
		_heading = player.getHeading();
		_pos = player.getInVehiclePosition();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
		writeD(_heading);
	}
}
