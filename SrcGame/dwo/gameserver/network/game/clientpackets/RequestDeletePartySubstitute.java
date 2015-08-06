package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.11
 * Time: 9:54
 */

// UC Data: native final function RequestDeletePartySubstitute (int UserID);
public class RequestDeletePartySubstitute extends L2GameClientPacket
{
	private int _userId;

	@Override
	protected void readImpl()
	{
		_userId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		_log.log(Level.INFO, "[IMPLEMENT ME!] RequestDeletePartySubstitute: UserID: " + _userId);
	}

	@Override
	public String getType()
	{
		return "[C] D0:A9 RequestDeletePartySubstitute";
	}
}
