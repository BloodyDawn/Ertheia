package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

public class ExReplyReceivedPost extends L2GameServerPacket
{
	private MailMessage _msg;
	private L2ItemInstance[] _items;

	public ExReplyReceivedPost(MailMessage msg)
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
		writeD(_msg.getType());
		// Предмет был возвращен из-за истечения срока
		if(_msg.getType() == 4)
		{
			writeD(3492);
			writeD(3493);
		}
		// Предмет был продан
		else if(_msg.getType() == 5)
		{
			writeD(_msg.getItemId());
			writeD(_msg.getEnchantLvl());
			for(int i = 0; i < 6; i++)
			{
				writeD(_msg.getElements()[i]);
			}
			writeD(3490);
			writeD(3491);
		}
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeD(0x00); // GOD
		writeS(_msg.getSenderName());
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
			_items = null;
		}
		else
		{
			writeD(0x00);
		}

		writeQ(_msg.getReqAdena());
		writeD(_msg.hasAttachments() ? 1 : 0);
		writeD(_msg.isFourStars() ? 0x01 : 0x00);

		_msg = null;
	}
}
