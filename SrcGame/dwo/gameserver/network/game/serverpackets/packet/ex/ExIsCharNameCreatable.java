package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi, ANZO
 * Date: 19.05.2011
 * Time: 10:58:06
 */

public class ExIsCharNameCreatable extends L2GameServerPacket
{
	private int _isCharCreatable;

	public ExIsCharNameCreatable(int isCharCreatable)
	{
		_isCharCreatable = isCharCreatable;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_isCharCreatable); // 0x00 - чар существует, 0xFFFFFFFF - ник свободен
	}
}
