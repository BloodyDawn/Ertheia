package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExReplyPostItemList;

/**
 * @author Migi, DS
 */
public class RequestPostItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	public void runImpl()
	{
		if(!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		activeChar.sendPacket(new ExReplyPostItemList(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] D0:65 RequestPostItemList";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
