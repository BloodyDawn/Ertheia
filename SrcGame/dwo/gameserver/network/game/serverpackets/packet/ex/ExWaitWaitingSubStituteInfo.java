package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 19.10.11
 * Time: 19:41
 */

public class ExWaitWaitingSubStituteInfo extends L2GameServerPacket
{
	private int _ok;

	public ExWaitWaitingSubStituteInfo(int ok)
	{
		_ok = ok;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_ok);
	}
}

