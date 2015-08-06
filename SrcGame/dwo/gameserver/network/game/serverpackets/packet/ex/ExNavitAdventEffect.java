package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author mochitto
 */

public class ExNavitAdventEffect extends L2GameServerPacket
{
	private final int _timeLeft;

	public ExNavitAdventEffect(int timeLeft)
	{
		_timeLeft = timeLeft;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_timeLeft);
	}
}
