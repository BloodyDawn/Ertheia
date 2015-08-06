package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */

public class ExDuelEnd extends L2GameServerPacket
{
	private int _unk1;

	public ExDuelEnd(int unk1)
	{
		_unk1 = unk1;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_unk1);
	}
}
