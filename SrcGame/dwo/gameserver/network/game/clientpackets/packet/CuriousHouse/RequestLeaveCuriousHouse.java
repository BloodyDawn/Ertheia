package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.scripts.instances.ChaosFestival;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.12
 * Time: 22:51
 */
public class RequestLeaveCuriousHouse extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		if(getClient().getActiveChar() != null)
		{
			ChaosFestival.getInstance().exitChallenge(getClient().getActiveChar());
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:C5 RequestLeaveCuriousHouse";
	}
}
