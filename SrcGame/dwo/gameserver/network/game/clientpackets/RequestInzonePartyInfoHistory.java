package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * L2GOD Team
 * User: ANZO,Bacek
 * Date: 18.10.11
 * Time: 15:32
 */

public class RequestInzonePartyInfoHistory extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Триггер
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		// activeChar.sendPacket(new ExLoadInzonePartyHistory(activeChar.getObjectId()));
	}

	@Override
	public String getType()
	{
		return "[C] D0:9A RequestInzonePartyInfoHistory";
	}
}