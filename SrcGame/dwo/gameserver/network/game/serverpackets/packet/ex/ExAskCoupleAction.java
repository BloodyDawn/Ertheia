package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExAskCoupleAction extends L2GameServerPacket
{
	private int _charObjId;
	private int _actionId;

	public ExAskCoupleAction(int charObjId, int social)
	{
		_charObjId = charObjId;
		_actionId = social;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_actionId);
		writeD(_charObjId);
	}
}
