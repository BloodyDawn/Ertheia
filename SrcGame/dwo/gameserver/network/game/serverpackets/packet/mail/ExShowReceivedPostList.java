package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * @author Migi, DS, Bacek
 */
public class ExShowReceivedPostList extends L2GameServerPacket
{
	private List<MailMessage> _inbox;

	public ExShowReceivedPostList(int objectId)
	{
		_inbox = MailManager.getInstance().getInbox(objectId);
	}

	@Override
	protected void writeImpl()
	{
		writeD((int) (System.currentTimeMillis() / 1000));

		if(_inbox != null && !_inbox.isEmpty())
		{
			writeD(_inbox.size());
			for(MailMessage msg : _inbox)
			{
				writeD(msg.getType());

				// Предмет был возвращен из-за истечения срока
				if(msg.getType() == 4)
				{
					writeD(3492);
				}
				// Предмет был продан
				if(msg.getType() == 5)
				{
					writeD(3490);
				}

				writeD(msg.getId());
				writeS(msg.getSubject());
				writeS(msg.getSenderName());
				writeD(msg.isLocked() ? 0x01 : 0x00);
				writeD(msg.getExpirationSeconds());
				writeD(msg.isUnread() ? 0x01 : 0x00);
				writeD(msg.getType() == 4 || msg.getType() == 5 ? 0x00 : 0x01); // TODO: уточнить за что отвечает параметр
				writeD(msg.hasAttachments() ? 0x01 : 0x00);
				writeD(msg.isFourStars() ? 0x01 : 0x00);

				if(msg.getType() == 4 || msg.getType() == 5)
				{
					writeD(2730);
				}
				else
				{
					writeD(msg.isNews() ? 0x01 : 0x00);  //TODO: уточноить что приходит в обычном письме
				}
			}
		}
		else
		{
			writeD(0x00);
		}

		writeD(100);  // Commision  базавая цена отправки
		writeD(1000); // PerSlot  цена за 1 слот

		FastList.recycle((FastList<MailMessage>) _inbox);
		_inbox = null;
	}
}
