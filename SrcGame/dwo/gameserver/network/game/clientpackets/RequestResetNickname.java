package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author KenM
 */

public class RequestResetNickname extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// nothing (trigger)
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		activeChar.getAppearance().setTitleColor(0xFFFF77);
		activeChar.setTitle("");
		activeChar.broadcastTitleInfo();
	}

	@Override
	public String getType()
	{
		return "[C] D0:53 RequestResetNickname";
	}
}
