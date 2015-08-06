package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Object;

public class DeleteObject extends L2GameServerPacket
{
	private final int _objectId;

	public DeleteObject(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}

	public DeleteObject(int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(0x00); //c2
	}
}
