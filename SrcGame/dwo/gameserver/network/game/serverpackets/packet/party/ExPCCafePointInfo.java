package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private final int _points;
	private final int _mAddPoint;
    private final int _time;
    private int _mPeriodType;
	private int _remainTime;
	private int _pointType;

	public ExPCCafePointInfo()
	{
		_points = 0;
		_mAddPoint = 0;
		_remainTime = 0;
		_mPeriodType = 0;
		_pointType = 0;
        _time = 0;
	}

	public ExPCCafePointInfo(int points, int modify_points, boolean mod, boolean _double, int hours_left, int time)
	{
		_points = points;
		_mAddPoint = modify_points;
		_remainTime = hours_left;
		if(mod && _double)
		{
			_mPeriodType = 1;
			_pointType = 0;
		}
		else if(mod)
		{
			_mPeriodType = 1;
			_pointType = 1;
		}
		else
		{
			_mPeriodType = 2;
			_pointType = 2;
		}
        _time = time;
	}


	@Override
	protected void writeImpl()
	{
		writeD(_points); // num points
		writeD(_mAddPoint); // points inc display
		writeC(_mPeriodType); // period(0=don't show window,1=acquisition,2=use points)
		writeD(_remainTime); // period hours left
		writeC(_pointType); // points inc display color(0=yellow,1=cyan-blue,2=red,all other black)
        writeD(_time * 3);
	}
}
