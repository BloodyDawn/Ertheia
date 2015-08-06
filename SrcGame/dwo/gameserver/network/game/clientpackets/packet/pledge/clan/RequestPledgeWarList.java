package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeReceiveWarList;

/**
 * @author  -Wooden-
 */

public class RequestPledgeWarList extends L2GameClientPacket
{
	private int _page;
	private int _state;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_state = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getClan() == null)
		{
			return;
		}

		//do we need powers to do that??
		activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(), _page, _state));
	}

	@Override
	public String getType()
	{
		return "[C] D0:1E RequestPledgeWarList";
	}
}