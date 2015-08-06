package dwo.gameserver.network.game.serverpackets.packet.vehicle.airship;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author kerberos
 * JIV update 27.8.10
 */

public class ExStopMoveInAirShip extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int _shipObjId;
	private int x;
	private int y;
	private int z;
	private int h;

	public ExStopMoveInAirShip(L2PcInstance player, int shipObjId)
	{
		_activeChar = player;
		_shipObjId = shipObjId;
		x = player.getInVehiclePosition().getX();
		y = player.getInVehiclePosition().getY();
		z = player.getInVehiclePosition().getZ();
		h = player.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(_shipObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(h);
	}
}
