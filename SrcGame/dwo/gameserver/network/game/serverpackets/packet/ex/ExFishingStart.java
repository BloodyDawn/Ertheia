package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author -Wooden-
 */

public class ExFishingStart extends L2GameServerPacket
{
	private L2Character _activeChar;
	private int _x;
	private int _y;
	private int _z;
	private int _fishType;
	private boolean _isNightLure;

	public ExFishingStart(L2Character character, int fishType, int x, int y, int z, boolean isNightLure)
	{
		_activeChar = character;
		_fishType = fishType;
		_x = x;
		_y = y;
		_z = z;
		_isNightLure = isNightLure;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeC(_fishType); // fish type
		writeD(_x); // x position
		writeD(_y); // y position
		writeD(_z); // z position
		writeC(_isNightLure ? 0x01 : 0x00); // night lure
	}
}