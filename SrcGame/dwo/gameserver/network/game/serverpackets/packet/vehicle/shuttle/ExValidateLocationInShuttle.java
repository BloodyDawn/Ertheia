package dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.07.11
 * Time: 15:32
 */

public class ExValidateLocationInShuttle extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int shuttleId;
	private int x;
	private int y;
	private int z;
	private int h;

	public ExValidateLocationInShuttle(L2PcInstance player)
	{
		_activeChar = player;
		shuttleId = _activeChar.getShuttle().getId();
		x = player.getInVehiclePosition().getX();
		y = player.getInVehiclePosition().getY();
		z = player.getInVehiclePosition().getZ();
		h = player.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(shuttleId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(h);
	}
}
