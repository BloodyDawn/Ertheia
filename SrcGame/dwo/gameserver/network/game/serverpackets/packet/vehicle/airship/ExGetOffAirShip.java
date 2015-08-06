package dwo.gameserver.network.game.serverpackets.packet.vehicle.airship;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExGetOffAirShip extends L2GameServerPacket
{
	private final int _playerId;
	private final int _airShipId;
	private final int _x;
	private final int _y;
	private final int _z;

	public ExGetOffAirShip(L2Character player, L2Character ship, int x, int y, int z)
	{
		_playerId = player.getObjectId();
		_airShipId = ship.getObjectId();
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_playerId);
		writeD(_airShipId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}