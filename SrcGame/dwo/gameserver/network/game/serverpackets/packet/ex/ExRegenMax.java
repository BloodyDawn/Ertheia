package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExRegenMax extends L2GameServerPacket
{
	private double _max;
	private int _count;
	private int _time;

	public ExRegenMax(double max, int count, int time)
	{
		_max = max;
		_count = count;
		_time = time;
	}

	@Override
	protected void writeImpl()
	{
		writeD(1);
		writeD(_count);
		writeD(_time);
		writeF(_max);
	}
}
