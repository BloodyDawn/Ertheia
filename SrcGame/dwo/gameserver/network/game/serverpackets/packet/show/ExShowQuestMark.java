package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Luca Baldi, ANZO
 */

public class ExShowQuestMark extends L2GameServerPacket
{
	private int _questId;
	private int _cond;

	public ExShowQuestMark(int questId, int cond)
	{
		_questId = questId;
		_cond = cond;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_questId);
		writeD(_cond);
	}
}
