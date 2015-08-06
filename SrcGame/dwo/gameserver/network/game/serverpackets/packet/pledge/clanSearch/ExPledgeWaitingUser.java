package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:27
 */
public class ExPledgeWaitingUser extends L2GameServerPacket
{
	private final int _charId;
	private final String _desc;

	public ExPledgeWaitingUser(int charId, String desc)
	{
		_charId = charId;
		_desc = desc;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charId);
		writeS(_desc);
		// dS
	}
}
