package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 23.12.12
 * Time: 14:17
 */
public class ExPledgeCount extends L2GameServerPacket
{
	private int _count;

	public ExPledgeCount(int count)
	{
		_count = count;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_count);
	}

}
