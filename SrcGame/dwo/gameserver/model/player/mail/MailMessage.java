/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.player.mail;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.queries.MailMessages;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.Mail;
import dwo.gameserver.util.Rnd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Migi, DS
 */
public class MailMessage
{
	private static final int EXPIRATION = 360; // 15 days
	private static final int COD_EXPIRATION = 12; // 12 hours

	private static final int UNLOAD_ATTACHMENTS_INTERVAL = 900000; // 15-30 mins

	private final int _messageId;
	private final int _senderId;
	private final int _receiverId;
	private final long _expiration;
	private final String _subject;
	private final String _content;
	private String _senderName;
	private String _receiverName;
	private boolean _unread;
	private boolean _fourStars;
	private boolean _news;
	private boolean _deletedBySender;
	private boolean _deletedByReceiver;
	private long _reqAdena;
	private boolean _hasAttachments;
	private Mail _attachments;
	private ScheduledFuture<?> _unloadTask;
	private boolean _isSentBySystem;

	private int _type;

	// Комиссионка (типы 4 и 5)
	private int _itemId;
	private long _sellAdena;
	private int _enchantLvl;
	private int[] _elementals = new int[6];

	/*
	 * Constructor for restoring from DB.
	 */
	public MailMessage(ResultSet rset) throws SQLException
	{
		_messageId = rset.getInt("messageId");
		_senderId = rset.getInt("senderId");
		_receiverId = rset.getInt("receiverId");
		_subject = rset.getString("subject");
		_content = rset.getString("content");
		_expiration = rset.getLong("expiration");
		_reqAdena = rset.getLong("reqAdena");
		_hasAttachments = rset.getBoolean("hasAttachments");
		_unread = rset.getBoolean("isUnread");
		_deletedBySender = rset.getBoolean("isDeletedBySender");
		_deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
		_fourStars = rset.getBoolean("isFourStars");
		_news = rset.getBoolean("isNews");
		_isSentBySystem = _senderId == -1;

		_type = rset.getInt("type");
		_itemId = rset.getInt("itemId");
		_sellAdena = rset.getLong("sellAdena");
		_enchantLvl = rset.getInt("enchantLvl");
		String[] elemDef = rset.getString("elementals").split(";");
		for(int i = 0; i < 6; i++)
		{
			_elementals[i] = Byte.valueOf(elemDef[i]);
		}
	}

	/*
	 * Конструктор, используемый для создания обычного сообщения
	 */
	public MailMessage(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = senderId;
		_receiverId = receiverId;
		_subject = subject;
		_content = text;
		_expiration = isCod ? System.currentTimeMillis() + COD_EXPIRATION * 3600000 : System.currentTimeMillis() + EXPIRATION * 3600000;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = false;
		_deletedByReceiver = false;
		_reqAdena = reqAdena;
		_isSentBySystem = _senderId == -1;
	}

	/*
	 * Конструктор, используемый для создания сообщения из комиссонки
	 */
	public MailMessage(int receiverId, String subject, CommissionItemHolder lot, L2ItemInstance item, int type)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		if(type == 4)
		{
			_content = "CommissionDeleteContent";
		}
		else
		{
			_content = lot.getItemName();
			if(item.isArmor())
			{
				for(int i = 0; i < 6; i++)
				{
					_elementals[i] = item.getElementDefAttr((byte) i);
				}
			}
			else if(item.isWeapon())
			{
				if(item.getAttackElementType() >= 0)
				{
					_elementals[item.getAttackElementType()] = item.getAttackElementPower();
				}
			}
		}
		_expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		_hasAttachments = false;
		_unread = true;
		_fourStars = false;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_reqAdena = 0;
		_senderName = lot.getCharName();
		_isSentBySystem = true;

		_type = type;
		_sellAdena = lot.getPrice();
		_enchantLvl = lot.getEnchantLevel();
	}

	/*
	 * This constructor is used for creating new System message
	 */
	public MailMessage(int receiverId, String subject, String content, String sendBy)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_reqAdena = 0;
		_senderName = sendBy;
		_isSentBySystem = true;
	}

	/*
	 * This constructor used for auto-generation of the "return attachments" message
	 */
	public MailMessage(MailMessage msg)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = msg._senderId;
		_receiverId = msg._senderId;
		_subject = "";
		_content = "";
		_expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_fourStars = true;
		_reqAdena = 0;
		_hasAttachments = true;
		_attachments = msg.getAttachments();
		msg.removeAttachments();
		_attachments.setNewMessageId(_messageId);
		_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this), UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		_isSentBySystem = _senderId == -1;
	}

	public static FiltredPreparedStatement getStatement(MailMessage msg, ThreadConnection con) throws SQLException
	{
		FiltredPreparedStatement stmt = con.prepareStatement(MailMessages.INSERT);

		stmt.setInt(1, msg._messageId);
		stmt.setInt(2, msg._senderId);
		stmt.setInt(3, msg._receiverId);
		stmt.setString(4, msg._subject);
		stmt.setString(5, msg._content);
		stmt.setLong(6, msg._expiration);
		stmt.setLong(7, msg._reqAdena);
		stmt.setString(8, String.valueOf(msg._hasAttachments));
		stmt.setString(9, String.valueOf(msg._unread));
		stmt.setString(10, String.valueOf(msg._deletedBySender));
		stmt.setString(11, String.valueOf(msg._deletedByReceiver));
		stmt.setString(12, String.valueOf(msg._fourStars));
		stmt.setString(13, String.valueOf(msg._news));
		// Комиссионка
		stmt.setString(14, String.valueOf(msg._type));
		stmt.setString(15, String.valueOf(msg._itemId));
		stmt.setString(16, String.valueOf(msg._sellAdena));
		stmt.setString(17, String.valueOf(msg._enchantLvl));
		stmt.setString(18, msg._elementals[0] + ";" + msg._elementals[1] + ';' + msg._elementals[2] + ';' + msg._elementals[3] + ';' + msg._elementals[4] + ';' + msg._elementals[5]);
		return stmt;
	}

	public int getId()
	{
		return _messageId;
	}

	public int getSenderId()
	{
		return _senderId;
	}

	public int getReceiverId()
	{
		return _receiverId;
	}

	public String getSenderName()
	{
		if(_senderName == null)
		{
			if(_fourStars)
			{
				return "****";
			}

			if(_senderId == -1)
			{
				_senderName = "Система";
				return _senderName;
			}

			_senderName = CharNameTable.getInstance().getNameById(_senderId);
			if(_senderName == null)
			{
				_senderName = "";
			}
		}
		return _senderName;
	}

	public String getReceiverName()
	{
		if(_receiverName == null)
		{
			_receiverName = CharNameTable.getInstance().getNameById(_receiverId);
			if(_receiverName == null)
			{
				_receiverName = "";
			}
		}
		return _receiverName;
	}

	public String getSubject()
	{
		return _subject;
	}

	public String getContent()
	{
		return _content;
	}

	public boolean isLocked()
	{
		return _reqAdena > 0;
	}

	public long getExpiration()
	{
		return _expiration;
	}

	public int getExpirationSeconds()
	{
		return (int) (_expiration / 1000);
	}

	public boolean isUnread()
	{
		return _unread;
	}

	public void markAsRead()
	{
		if(_unread)
		{
			_unread = false;
			MailManager.getInstance().markAsReadedInDb(_messageId);
		}
	}

	public boolean isDeletedBySender()
	{
		return _deletedBySender;
	}

	public void setDeletedBySender()
	{
		if(!_deletedBySender)
		{
			_deletedBySender = true;
			if(_deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(_messageId);
			}
		}
	}

	public boolean isDeletedByReceiver()
	{
		return _deletedByReceiver;
	}

	public void setDeletedByReceiver()
	{
		if(!_deletedByReceiver)
		{
			_deletedByReceiver = true;
			if(_deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(_messageId);
			}
		}
	}

	public boolean isFourStars()
	{
		return _fourStars;
	}

	public boolean isNews()
	{
		return _news;
	}

	public void setIsNews(boolean val)
	{
		_news = val;
	}

	public long getReqAdena()
	{
		return _reqAdena;
	}

	public Mail getAttachments()
	{
		synchronized(this)
		{
			if(!_hasAttachments)
			{
				return null;
			}

			if(_attachments == null)
			{
				_attachments = new Mail(_senderId, _messageId);
				_attachments.restore();
				_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this), UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
			}
			return _attachments;
		}
	}

	public boolean hasAttachments()
	{
		return _hasAttachments;
	}

	public void removeAttachments()
	{
		synchronized(this)
		{
			if(_attachments != null)
			{
				_attachments = null;
				_hasAttachments = false;
				MailManager.getInstance().removeAttachmentsInDb(_messageId);
				if(_unloadTask != null)
				{
					_unloadTask.cancel(false);
				}
			}
		}
	}

	public Mail createAttachments()
	{
		synchronized(this)
		{
			if(_hasAttachments || _attachments != null)
			{
				return null;
			}

			_attachments = new Mail(_senderId, _messageId);
			_hasAttachments = true;
			_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this), UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
			return _attachments;
		}
	}

	protected void unloadAttachments()
	{
		synchronized(this)
		{
			if(_attachments != null)
			{
				_attachments.deleteMe();
				_attachments = null;
			}
		}
	}

	/***
	 * Отправка текущего письма
	 */
	public void sendMessage()
	{
		MailManager.getInstance().sendMessage(this);
	}

	public boolean isSentBySystem()
	{
		return _isSentBySystem;
	}

	public int getType()
	{
		return _type;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getSellAdena()
	{
		return _sellAdena;
	}

	public int getEnchantLvl()
	{
		return _enchantLvl;
	}

	public int[] getElements()
	{
		return _elementals;
	}

	static class AttachmentsUnloadTask implements Runnable
	{
		private MailMessage _msg;

		AttachmentsUnloadTask(MailMessage msg)
		{
			_msg = msg;
		}

		@Override
		public void run()
		{
			if(_msg != null)
			{
				_msg.unloadAttachments();
				_msg = null;
			}
		}
	}
}
