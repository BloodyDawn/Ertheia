package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi, ANZO
 * Date: 24.05.2011
 * Time: 11:22:18
 */

public class ExChangeToAwakenedClass extends L2GameServerPacket
{
	private int _classId;

	public ExChangeToAwakenedClass(int classid)
	{
		_classId = classid;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_classId);
	}
}
