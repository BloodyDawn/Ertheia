package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExChangeNPCState extends L2GameServerPacket
{
	private final int _objId;
	private final int _state;

	public ExChangeNPCState(int objId, int state)
	{
		_objId = objId;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeD(_state);
	}
}
