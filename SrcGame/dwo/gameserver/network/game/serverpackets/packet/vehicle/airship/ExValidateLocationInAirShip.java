package dwo.gameserver.network.game.serverpackets.packet.vehicle.airship;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author kerberos
 * JIV update 27.8.10
 */

public class ExValidateLocationInAirShip extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int shipId;
	private int x;
	private int y;
	private int z;
	private int h;

	public ExValidateLocationInAirShip(L2PcInstance player)
	{
		_activeChar = player;
		shipId = _activeChar.getAirShip().getObjectId();
		x = player.getInVehiclePosition().getX();
		y = player.getInVehiclePosition().getY();
		z = player.getInVehiclePosition().getZ();
		h = player.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(shipId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(h);
	}
}
