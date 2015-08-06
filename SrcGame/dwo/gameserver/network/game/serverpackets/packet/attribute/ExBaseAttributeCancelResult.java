package dwo.gameserver.network.game.serverpackets.packet.attribute;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExBaseAttributeCancelResult extends L2GameServerPacket
{
	private int _objId;
	private byte _attribute;

	public ExBaseAttributeCancelResult(int objId, byte attribute)
	{
		_objId = objId;
		_attribute = attribute;
	}

	@Override
	protected void writeImpl()
	{
		writeD(1); // result
		writeD(_objId);
		writeD(_attribute);
	}
}
