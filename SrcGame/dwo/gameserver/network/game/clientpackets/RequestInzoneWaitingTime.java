package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExInzoneWaitingInfo;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 11.07.12
 * Time: 20:43
 */
public class RequestInzoneWaitingTime extends L2GameClientPacket
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
			return;
		}

		activeChar.sendPacket(new ExInzoneWaitingInfo(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] D0:C2 RequestInzoneWaitingTime";
	}
}


