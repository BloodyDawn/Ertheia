package dwo.gameserver.network.game.serverpackets;

public class PlaySound extends L2GameServerPacket
{
	private int _unknown1;
	private String _soundFile;
	private int _isForShip;
	private int _objectId;
	private int _x;
	private int _y;
	private int _z;
	private int _unknown8;

	public PlaySound(String soundFile)
	{
		_unknown1 = 0;
		_soundFile = soundFile;
		_isForShip = 0;
		_objectId = 0;
		_x = 0;
		_y = 0;
		_z = 0;
		_unknown8 = 0;
	}

	public PlaySound(int unknown1, String soundFile, int unknown3, int unknown4, int x, int y, int z)
	{
		_unknown1 = unknown1;
		_soundFile = soundFile;
		_isForShip = unknown3;
		_objectId = unknown4;
		_x = x;
		_y = y;
		_z = z;
		_unknown8 = 0;
	}

	public String getSoundName()
	{
		return _soundFile;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_unknown1);              //unknown 0 for quest and ship;
		writeS(_soundFile);
		writeD(_isForShip);              //unknown 0 for quest; 1 for ship;
		writeD(_objectId);              //0 for quest; objectId of ship
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_unknown8);
	}
}
