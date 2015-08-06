package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExRotation extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _degree;

	public ExRotation(int charId, int degree)
	{
		_charObjId = charId;
		_degree = degree;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_degree);
	}
}
