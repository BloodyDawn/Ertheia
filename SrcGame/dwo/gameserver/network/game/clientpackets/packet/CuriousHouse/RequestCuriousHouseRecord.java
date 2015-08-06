package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.12
 * Time: 23:04
 */
public class RequestCuriousHouseRecord extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Ничего
	}

	@Override
	protected void runImpl()
	{

	}

	@Override
	public String getType()
	{
		return "[C] D0:CA RequestCuriousHouseRecord";
	}
}
