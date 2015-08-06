package dwo.gameserver.network.game.serverpackets.packet.event;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: Bacek
 * Date: 03.02.13
 * Time: 22:02
 */
public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket
{
	private int _snow;

	private ExBR_NewIConCashBtnWnd(int snow)
	{
		_snow = snow;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_snow);
	}
}
