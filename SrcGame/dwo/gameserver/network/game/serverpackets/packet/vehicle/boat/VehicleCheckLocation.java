package dwo.gameserver.network.game.serverpackets.packet.vehicle.boat;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Maktakien
 */

public class VehicleCheckLocation extends L2GameServerPacket
{
	private L2Character _boat;

	public VehicleCheckLocation(L2Character boat)
	{
		_boat = boat;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_boat.getObjectId());
		writeD(_boat.getX());
		writeD(_boat.getY());
		writeD(_boat.getZ());
		writeD(_boat.getHeading());
	}
}
