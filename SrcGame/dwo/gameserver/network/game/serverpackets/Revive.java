package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Object;

public class Revive extends L2GameServerPacket
{
	private int _objectId;

	public Revive(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
	}
}
