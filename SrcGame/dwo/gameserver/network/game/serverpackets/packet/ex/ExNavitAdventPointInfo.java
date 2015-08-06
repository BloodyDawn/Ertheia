package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExNavitAdventPointInfo extends L2GameServerPacket
{
	private final int _points;

	public ExNavitAdventPointInfo(int points)
	{
		_points = points;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_points); // 72 = 1%
	}
}
