package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

/**
 * @author Migi, DS
 */

public class ExReplySentPost extends L2GameServerPacket
{
	private MailMessage _msg;
	private L2ItemInstance[] _items;

	public ExReplySentPost(MailMessage msg)
	{
		_msg = msg;
		if(msg.hasAttachments())
		{
			ItemContainer attachments = msg.getAttachments();
			if(attachments != null && attachments.getSize() > 0)
			{
				_items = attachments.getItems();
			}
			else
			{
				_log.log(Level.WARN, "Message " + msg.getId() + " has attachments but itemcontainer is empty.");
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		// dddSSSd
		writeD(0x00); // GOD
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeS(_msg.getReceiverName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());

		if(_items != null && _items.length > 0)
		{
			writeD(_items.length);
			for(L2ItemInstance item : _items)
			{
				writeItemInfo(item);
				writeD(item.getObjectId());
			}
			writeQ(_msg.getReqAdena());
			writeD(_msg.isFourStars() ? 0x01 : 0x00);
		}
		else
		{
			writeD(0x00);
		}

		_items = null;
		_msg = null;
	}
}
