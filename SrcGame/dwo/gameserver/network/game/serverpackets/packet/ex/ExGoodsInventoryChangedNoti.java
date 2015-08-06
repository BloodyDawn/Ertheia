package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 23.06.12
 * Time: 13:21
 */

public class ExGoodsInventoryChangedNoti extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeC(0x01);
	}
}