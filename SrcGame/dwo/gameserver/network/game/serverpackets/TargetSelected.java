package dwo.gameserver.network.game.serverpackets;

public class TargetSelected extends L2GameServerPacket
{
	private int _objectId;
	private int _targetObjId;
	private int _x;
	private int _y;
	private int _z;

	public TargetSelected(int objectId, int targetId, int x, int y, int z)
	{
		_objectId = objectId;
		_targetObjId = targetId;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00);
	}
}
