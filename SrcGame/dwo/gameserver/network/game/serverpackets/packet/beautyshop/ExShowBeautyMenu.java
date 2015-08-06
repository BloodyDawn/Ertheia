package dwo.gameserver.network.game.serverpackets.packet.beautyshop;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 17.11.12
 * Time: 12:23
 */
public class ExShowBeautyMenu extends L2GameServerPacket
{
	public static int ADD;
	public static int REMOVE = 1;

	private int _type;

	public ExShowBeautyMenu(int type)
	{
		_type = type;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
	}
}
