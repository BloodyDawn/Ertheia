package dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.07.11
 * Time: 15:20
 */

public class ExSuttleGetOff extends L2GameServerPacket
{
	private final int _playerId;
	private final int _shuttleId;
	private final int _x;
	private final int _y;
	private final int _z;

	public ExSuttleGetOff(int playerId, int shuttleId, int x, int y, int z)
	{
		_playerId = playerId;
		_shuttleId = shuttleId;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_playerId);
		writeD(_shuttleId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
