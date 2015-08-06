package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ShowCalc extends L2GameServerPacket
{
	private int _calculatorId;

	public ShowCalc(int calculatorId)
	{
		_calculatorId = calculatorId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_calculatorId);
	}
}