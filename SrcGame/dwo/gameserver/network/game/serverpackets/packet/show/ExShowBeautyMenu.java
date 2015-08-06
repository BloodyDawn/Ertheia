package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 15.10.12
 * Time: 16:48
 * L2GOD Team/
 * Info: Пакет тригер, включает "Салон красоты". 0 выкл, 1 вкл.
 */
public class ExShowBeautyMenu extends L2GameServerPacket
{
	private int _beauty;

	public ExShowBeautyMenu(int beauty)
	{
		_beauty = beauty;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_beauty);
	}
}
