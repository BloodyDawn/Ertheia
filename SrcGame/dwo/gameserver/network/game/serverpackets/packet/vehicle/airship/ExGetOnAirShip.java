package dwo.gameserver.network.game.serverpackets.packet.vehicle.airship;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

public class ExGetOnAirShip extends L2GameServerPacket
{
	private final int _playerId;
	private final int _airShipId;
	private final Point3D _pos;

	public ExGetOnAirShip(L2PcInstance player, L2Character ship)
	{
		_playerId = player.getObjectId();
		_airShipId = ship.getObjectId();
		_pos = player.getInVehiclePosition();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_playerId);
		writeD(_airShipId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
	}
}