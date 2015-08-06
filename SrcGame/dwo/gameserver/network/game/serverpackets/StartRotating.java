package dwo.gameserver.network.game.serverpackets;

public class StartRotating extends L2GameServerPacket
{
	private int _charObjId;
	private int _degree;
	private int _side;
	private int _speed;

	public StartRotating(int objectId, int degree, int side, int speed)
	{
		_charObjId = objectId;
		_degree = degree;
		_side = side;
		_speed = speed;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_degree);
		writeD(_side);
		writeD(_speed);
	}
}
