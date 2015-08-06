package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * @author Migi, DS
 */

public class ExShowSentPostList extends L2GameServerPacket
{
	private List<MailMessage> _outbox;

	public ExShowSentPostList(int objectId)
	{
		_outbox = MailManager.getInstance().getOutbox(objectId);
	}

	@Override
	protected void writeImpl()
	{
		writeD((int) (System.currentTimeMillis() / 1000));
		if(_outbox != null && !_outbox.isEmpty())
		{
			writeD(_outbox.size());
			for(MailMessage msg : _outbox)
			{
				writeD(msg.getId());
				writeS(msg.getSubject());
				writeS(msg.getReceiverName());
				writeD(msg.isLocked() ? 0x01 : 0x00);
				writeD(msg.getExpirationSeconds());
				writeD(msg.isUnread() ? 0x01 : 0x00);
				writeD(0x01);
				writeD(msg.hasAttachments() ? 0x01 : 0x00);
				writeD(0x00);
			}
		}
		else
		{
			writeD(0x00);
		}
		FastList.recycle((FastList<MailMessage>) _outbox);
		_outbox = null;
	}
}
