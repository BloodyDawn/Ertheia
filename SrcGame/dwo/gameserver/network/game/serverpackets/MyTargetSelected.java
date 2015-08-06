package dwo.gameserver.network.game.serverpackets;

public class MyTargetSelected extends L2GameServerPacket
{
	private int _objectId;
	private int _color;

	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeH(_color);
		writeD(0x00);
	}
}
