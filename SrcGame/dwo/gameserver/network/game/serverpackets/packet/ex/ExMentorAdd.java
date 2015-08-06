package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.10.11
 * Time: 9:28
 */

public class ExMentorAdd extends L2GameServerPacket
{
	final L2PcInstance _mentor;

	public ExMentorAdd(L2PcInstance mentor)
	{
		_mentor = mentor;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_mentor.getName());
		writeD(_mentor.getClassId().getId());
		writeD(_mentor.getLevel());
	}
}
