package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;

public class TargetUnselected extends L2GameServerPacket
{
	private int _targetObjId;
	private int _x;
	private int _y;
	private int _z;

	public TargetUnselected(L2Character character)
	{
		_targetObjId = character.getObjectId();
		_x = character.getX();
		_y = character.getY();
		_z = character.getZ();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00); //??
	}
}
