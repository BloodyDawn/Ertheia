package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * @author Bacek
 * Date: 05.06.13
 * Time: 19:28
 */

public class ExSetPledgeEmblemAck extends L2GameServerPacket
{
	private int _part;

	public ExSetPledgeEmblemAck(int part)
	{
		_part = part;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_part);
	}
}