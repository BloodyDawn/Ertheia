package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

public class SAP extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isTeleporting())
		{
			activeChar.onTeleported();
		}

		activeChar.sendUserInfo();
	}

	@Override
	public String getType()
	{
		return "[C] 30 Appearing";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}