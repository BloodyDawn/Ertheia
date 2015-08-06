package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExOlympiadMode extends L2GameServerPacket
{
	// chc
	private int _mode;

	/**
	 * @param mode (0 = return, 3 = spectate)
	 */
	public ExOlympiadMode(int mode)
	{
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_mode);
	}
}
