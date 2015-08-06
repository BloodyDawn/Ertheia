package dwo.gameserver.network.game.serverpackets;

public class EarthQuake extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _intensity;
	private int _duration;

	public EarthQuake(int x, int y, int z, int intensity, int duration)
	{
		_x = x;
		_y = y;
		_z = z;
		_intensity = intensity;
		_duration = duration;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_intensity);
		writeD(_duration);
		writeD(0x00);       // TODO
	}
}
