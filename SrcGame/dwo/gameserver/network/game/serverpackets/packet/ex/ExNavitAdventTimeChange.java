package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author mochitto
 */

public class ExNavitAdventTimeChange extends L2GameServerPacket
{
	private final boolean _paused;
	private final int _time;

	public ExNavitAdventTimeChange(int time)
	{
		_time = time > 240000 ? 240000 : time;
		_paused = _time < 1;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_paused ? 0x00 : 0x01);
		writeD(_time); // time in ms (16000 = 4mins = state quit)
	}
}
