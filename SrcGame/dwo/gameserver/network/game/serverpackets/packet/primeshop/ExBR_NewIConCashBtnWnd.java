package dwo.gameserver.network.game.serverpackets.packet.primeshop;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 25.12.12
 * Time: 0:34
 */
public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0x01); // 0 скрыть 1 показать
	}
}
