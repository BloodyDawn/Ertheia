package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 15.06.2011
 * Time: 9:41:25
 */

public class ExNotifyFlyMoveStart extends L2GameServerPacket
{
	public static final ExNotifyFlyMoveStart STATIC_PACKET = new ExNotifyFlyMoveStart();

	private ExNotifyFlyMoveStart()
	{
	}

	@Override
	protected void writeImpl()
	{
	}
}
