package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;

/**
 * @author devScarlet
 */

public class NickNameChanged extends L2GameServerPacket
{
	private String _title;
	private int _objectId;

	public NickNameChanged(L2Character cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeS(_title);
	}
}
