package dwo.gameserver.network.game.serverpackets.packet.vehicle.airship;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

public class ExMoveToLocationInAirShip extends L2GameServerPacket
{
	private int _charObjId;
	private int _airShipId;
	private Point3D _destination;
	private int _heading;

	public ExMoveToLocationInAirShip(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_airShipId = player.getAirShip().getObjectId();
		_destination = player.getInVehiclePosition();
		_heading = player.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_airShipId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_heading);
	}
}