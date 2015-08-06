package dwo.gameserver.network.game.serverpackets;

public class FinishRotating extends L2GameServerPacket
{
	private int _charObjId;
	private int _degree;
	private int _speed;

	public FinishRotating(int objectId, int degree, int speed)
	{
		_charObjId = objectId;
		_degree = degree;
		_speed = speed;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId);
		writeD(_degree);
		writeD(_speed);
		writeC(0); // ?
	}
}
