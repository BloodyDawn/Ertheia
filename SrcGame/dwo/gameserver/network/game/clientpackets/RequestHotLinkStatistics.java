package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 18.06.12
 * Time: 14:24
 */
public class RequestHotLinkStatistics extends L2GameClientPacket
{
	private int _type;

	@Override
	protected void readImpl()
	{
		_type = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		activeChar.sendMessage("Группа: " + _type);

	}

	@Override
	public String getType()
	{
		return "[C] 08 RequestHotLinkStatistics";
	}
}
