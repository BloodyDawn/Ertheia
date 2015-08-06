package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:29
 */

public class RequestFirstPlayStart extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Ничего
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
		}
		// _log.log(Level.INFO, "[IMPLEMENT ME!] RequestFirstPlayStart");
	}

	@Override
	public String getType()
	{
		return "[C] D0:B3 RequestFirstPlayStart";
	}
}