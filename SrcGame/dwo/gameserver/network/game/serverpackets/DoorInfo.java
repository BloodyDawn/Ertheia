package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2DoorInstance;

public class DoorInfo extends L2GameServerPacket
{
	private final L2DoorInstance _door;

	public DoorInfo(L2DoorInstance door)
	{
		_door = door;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_door.getObjectId());
		writeD(_door.getDoorId());
	}
}
