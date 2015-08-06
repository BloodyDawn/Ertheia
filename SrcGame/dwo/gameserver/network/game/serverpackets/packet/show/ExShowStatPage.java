package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExShowStatPage extends L2GameServerPacket
{
	private final int _page;

	public ExShowStatPage(int page)
	{
		_page = page;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_page);
	}
}
