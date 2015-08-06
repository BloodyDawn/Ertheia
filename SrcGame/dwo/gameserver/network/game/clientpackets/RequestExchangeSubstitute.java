package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.10.11
 * Time: 8:23
 */

// UC Data: native final function RequestExchangeSubstitute (int partyMasterServerID, int partyChangeMemberServerID, int waitingPlayerServerID);
public class RequestExchangeSubstitute extends L2GameClientPacket
{
	private int partyMasterServerID;
	private int partyChangeMemberServerID;
	private int waitingPlayerServerID;

	@Override
	protected void readImpl()
	{
		partyMasterServerID = readD();
		partyChangeMemberServerID = readD();
		waitingPlayerServerID = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		_log.log(Level.DEBUG, "[IMPLEMENT ME!] RequestExchangeSubstitute: partyMasterServerID:" + partyMasterServerID + " partyChangeMemberServerID:" + partyChangeMemberServerID + " waitingPlayerServerID:" + waitingPlayerServerID);
	}

	@Override
	public String getType()
	{
		return "[C] D0:BA RequestExchangeSubstitute";
	}
}
