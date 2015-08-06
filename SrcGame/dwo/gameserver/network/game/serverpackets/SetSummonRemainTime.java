package dwo.gameserver.network.game.serverpackets;

public class SetSummonRemainTime extends L2GameServerPacket
{
	private int _maxTime;
	private int _remainingTime;

	public SetSummonRemainTime(int maxTime, int remainingTime)
	{
		_remainingTime = remainingTime;
		_maxTime = maxTime;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_maxTime);
		writeD(_remainingTime);
	}
}
