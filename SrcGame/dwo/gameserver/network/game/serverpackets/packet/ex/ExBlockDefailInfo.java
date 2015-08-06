package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class ExBlockDefailInfo extends L2GameServerPacket
{
	private final String _blockedCharName;
	private final String _memoText;

	public ExBlockDefailInfo(String blockedCharName, String memoText)
	{
		_blockedCharName = blockedCharName;
		_memoText = memoText;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_blockedCharName);
		writeS(_memoText);
	}
}
