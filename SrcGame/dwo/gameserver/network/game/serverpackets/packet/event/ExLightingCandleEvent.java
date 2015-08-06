package dwo.gameserver.network.game.serverpackets.packet.event;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 25.12.12
 * Time: 0:40
 */
public class ExLightingCandleEvent extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0x00);  // 0 скрыть 1 показать
	}
}
