package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExBR_PremiumState extends L2GameServerPacket
{
	private final int _objId;
	private final int _state;

	public ExBR_PremiumState(int id, int state)
	{
		_objId = id;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeC(_state);
	}
}