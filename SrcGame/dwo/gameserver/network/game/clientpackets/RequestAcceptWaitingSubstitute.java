package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.11
 * Time: 9:50
 */

// UC Data: native final function RequestAcceptWaitingSubstitute (int admission, int partyID, int UserID);
public class RequestAcceptWaitingSubstitute extends L2GameClientPacket
{
	int admission;
	int partyID;
	int userID;

	@Override
	protected void readImpl()
	{
		admission = readD();
		partyID = readD();
		userID = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		_log.log(Level.INFO, "[IMPLEMENT ME!] RequestAcceptWaitingSubstitute: admission: " + admission + " partyID: " + partyID + " userID: " + userID);
	}

	@Override
	public String getType()
	{
		return "[C] D0:96:02 RequestAcceptWaitingSubstitute";
	}
}