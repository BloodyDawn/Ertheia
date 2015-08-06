package dwo.gameserver.network.game.serverpackets.packet.trade;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class TradeDone extends L2GameServerPacket
{
	private int _num;

	public TradeDone(int num)
	{
		_num = num;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_num);
	}
}
