package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author GodKratos
 */

public class ExOlympiadMatchEnd extends L2GameServerPacket
{
	public static final ExOlympiadMatchEnd STATIC_PACKET = new ExOlympiadMatchEnd();

	private ExOlympiadMatchEnd()
	{
	}

	@Override
	protected void writeImpl()
	{
	}
}