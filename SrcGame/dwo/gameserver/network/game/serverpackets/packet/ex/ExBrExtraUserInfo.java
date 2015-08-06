package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Kerberos
 */

public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private final int _charObjId;

	public ExBrExtraUserInfo(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charObjId); //object ID of Player
		writeD(0x00);        // event effect id
		writeC(0x00);        // Event flag, added only if event is active
	}
}
