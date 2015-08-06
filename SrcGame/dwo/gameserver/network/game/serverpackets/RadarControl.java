package dwo.gameserver.network.game.serverpackets;

public class RadarControl extends L2GameServerPacket
{
	private int _showRadar;
	private int _type;
	private int _x;
	private int _y;
	private int _z;

	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar;         // showRader?? 0 = showradar; 1 = delete radar;
		_type = type;                   // radar type??
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_showRadar);
		writeD(_type);     //maybe type
		writeD(_x);    //x
		writeD(_y);    //y
		writeD(_z);    //z
	}
}
