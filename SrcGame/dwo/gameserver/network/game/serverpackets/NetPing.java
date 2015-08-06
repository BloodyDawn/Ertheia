package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.12
 * Time: 18:19
 */

public class NetPing extends L2GameServerPacket
{
	private int _clientId;

	public NetPing(L2PcInstance cha)
	{
		// На офе непонятно что приходит, используем в данном случае ObjID персонажа
		_clientId = cha.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_clientId);
	}
}
