package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStatusUpdate extends L2GameServerPacket
{
	private L2DoorInstance _door;

	public DoorStatusUpdate(L2DoorInstance door)
	{
		_door = door;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_door.getObjectId());
		writeD(_door.isOpened() ? 0 : 1);
		writeD(_door.getDamage());
		writeD(_door.isEnemy() ? 1 : 0);
		writeD(_door.getDoorId());
		writeD((int) _door.getCurrentHp());
		writeD(_door.getMaxVisibleHp());
	}
}