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
import dwo.gameserver.datatables.sql.queries.MailMessages;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExNoticePostArrived;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExUnReadMailCount;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Migi, DS<br>
 */
public class MailManager
{
	private static Logger _log = LogManager.getLogger(MailManager.class);

	private L2TIntObjectHashMap<MailMessage> _messages = new L2TIntObjectHashMap<>();

	private MailManager()
	{
		load();
	}

	public static MailManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(MailMessages.SELECT);
			rset = statement.executeQuery();
			while(rset.next())
			{
				MailMessage msg = new MailMessage(rset);

				int msgId = msg.getId();
				_messages.put(msgId, msg);

				long expiration = msg.getExpiration();

				if(expiration < System.currentTimeMillis())
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), 10000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error loading from database:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.log(Level.INFO, "Mail Manager: Successfully loaded " + _messages.size() + " messages.");
	}

	public MailMessage getMessage(int msgId)
	{
		return _messages.get(msgId);
	}

	public MailMessage[] getMessages()
	{
		return _messages.values(new MailMessage[0]);
	}

	public boolean hasUnreadPost(L2PcInstance player)
	{
		int objectId = player.getObjectId();
		for(MailMessage msg : getMessages())
		{
			if(msg != null && msg.getReceiverId() == objectId && msg.isUnread())
			{
				return true;
			}
		}
		return false;
	}

	public int getInboxSize(int objectId)
	{
		int size = 0;
		for(MailMessage msg : getMessages())
		{
			if(msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver())
			{
				size++;
			}
		}
		return size;
	}

	public int getOutboxSize(int objectId)
	{
		int size = 0;
		for(MailMessage msg : getMessages())
		{
			if(msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender())
			{
				size++;
			}
		}
		return size;
	}

	public List<MailMessage> getInbox(int objectId)
	{
		List<MailMessage> inbox = FastList.newInstance();
		for(MailMessage msg : getMessages())
		{
			if(msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver())
			{
				inbox.add(msg);
			}
		}
		return inbox;
	}

	public List<MailMessage> getOutbox(int objectId)
	{
		List<MailMessage> outbox = new FastList<>();
		for(MailMessage msg : getMessages())
		{
			if(msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender())
			{
				outbox.add(msg);
			}
		}
		return outbox;
	}

	public void sendMessage(MailMessage msg)
	{
		_messages.put(msg.getId(), msg);

		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = MailMessage.getStatement(msg, con);
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error saving message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}

		L2PcInstance receiver = WorldManager.getInstance().getPlayer(msg.getReceiverId());
		if(receiver != null)
		{
			receiver.sendPacket(ExNoticePostArrived.valueOf(true));
			receiver.sendPacket(new ExUnReadMailCount(receiver));
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
	}

	public void markAsReadedInDb(int msgId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement(MailMessages.MARK_AS_READED);
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error marking as read message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}
	}

	public void markAsDeletedBySenderInDb(int msgId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement(MailMessages.MARK_AS_DELETED_BY_SENDER);
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error marking as deleted by sender message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}
	}

	public void markAsDeletedByReceiverInDb(int msgId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement(MailMessages.MARK_AS_DELETED_BY_RECIEVER);
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error marking as deleted by receiver message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}
	}

	public void removeAttachmentsInDb(int msgId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement(MailMessages.DELETE_ATTACHMENTS);
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error removing attachments in message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}
	}

	public void deleteMessageInDb(int msgId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement(MailMessages.ADD);
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Mail Manager: Error deleting message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}

		_messages.remove(msgId);
		IdFactory.getInstance().releaseId(msgId);
	}

	private static class SingletonHolder
	{
		protected static final MailManager _instance = new MailManager();
	}

	class MessageDeletionTask implements Runnable
	{
		final int _msgId;

		public MessageDeletionTask(int msgId)
		{
			_msgId = msgId;
		}

		@Override
		public void run()
		{
			MailMessage msg = getMessage(_msgId);
			if(msg == null)
			{
				return;
			}

			if(msg.hasAttachments())
			{
				try
				{
					L2PcInstance sender = WorldManager.getInstance().getPlayer(msg.getSenderId());
					if(sender != null)
					{
						msg.getAttachments().returnToWh(sender.getWarehouse());
						sender.sendPacket(SystemMessageId.MAIL_RETURNED);
					}
					else
					{
						msg.getAttachments().destroyAllItems(ProcessType.MAIL_ATTACH, null, null);
					}

					msg.getAttachments().deleteMe();
					msg.removeAttachments();

					L2PcInstance receiver = WorldManager.getInstance().getPlayer(msg.getReceiverId());
					if(receiver != null)
					{
						receiver.sendPacket(SystemMessageId.MAIL_RETURNED);
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Mail Manager: Error returning items:" + e.getMessage(), e);
				}
			}
			deleteMessageInDb(msg.getId());
		}
	}
}