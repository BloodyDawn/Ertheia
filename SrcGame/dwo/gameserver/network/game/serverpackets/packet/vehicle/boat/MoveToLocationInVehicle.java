package dwo.gameserver.network.game.serverpackets.packet.vehicle.boat;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

/**
 * @author Maktakien
 */

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatId;
	private Point3D _destination;
	private Point3D _origin;

	/**
	 * @param player
	 * @param destination
	 * @param origin
	 */
	public MoveToLocationInVehicle(L2PcInstance player, Point3D destination, Point3D origin)
	{
		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_origin.getX());
		writeD(_origin.getY());
		writeD(_origin.getZ());
	}
}
