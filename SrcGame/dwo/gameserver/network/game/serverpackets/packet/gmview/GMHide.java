package dwo.gameserver.network.game.serverpackets.packet.gmview;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

@Deprecated
public class GMHide extends L2GameServerPacket
{
	// cd
	private int _mode;

	public GMHide(int mode)
	{
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_mode);
	}
}
