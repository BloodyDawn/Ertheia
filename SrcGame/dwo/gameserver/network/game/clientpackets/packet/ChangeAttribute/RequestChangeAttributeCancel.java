package dwo.gameserver.network.game.clientpackets.packet.ChangeAttribute;

import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.11
 * Time: 20:47
 */
public class RequestChangeAttributeCancel extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{

	}

	@Override
	public String getType()
	{
		return "[C] d0:b9 RequestChangeAttributeCancel";
	}
}
