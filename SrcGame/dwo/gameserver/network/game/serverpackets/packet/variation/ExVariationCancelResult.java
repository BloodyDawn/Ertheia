package dwo.gameserver.network.game.serverpackets.packet.variation;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExVariationCancelResult extends L2GameServerPacket
{
	private int _result;

	public ExVariationCancelResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}
}