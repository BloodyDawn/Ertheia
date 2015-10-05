package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Mail;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.L2AccessLevel;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExReplyWritePost;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;

import static dwo.gameserver.model.actor.L2Character.ZONE_PEACE;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class RequestSendPost extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	private static final int MAX_RECV_LENGTH = 16;
	private static final int MAX_SUBJ_LENGTH = 128;
	private static final int MAX_TEXT_LENGTH = 512;
	private static final int MAX_ATTACHMENTS = 8;
	private static final int INBOX_SIZE = 240;
	private static final int OUTBOX_SIZE = 240;

	private static final int MESSAGE_FEE = 100;
	private static final int MESSAGE_FEE_PER_SLOT = 1000; // 100 adena message fee + 1000 per each item slot

	private String _receiver;
	private boolean _isCod;
	private String _subject;
	private String _text;
	private ItemHolder[] _items;
	private long _reqAdena;

	@Override
	protected void readImpl()
	{
		_receiver = readS();
		_isCod = readD() != 0;
		_subject = readS();
		_text = readS();

		int attachCount = readD();
		if(attachCount < 0 || attachCount > Config.MAX_ITEM_IN_PACKET || attachCount * BATCH_LENGTH + 8 != _buf.remaining())
		{
			return;
		}

		if(attachCount > 0)
		{
			_items = new ItemHolder[attachCount];
			for(int i = 0; i < attachCount; i++)
			{
				int objectId = readD();
				long count = readQ();
				if(objectId < 1 || count < 0)
				{
					_items = null;
					return;
				}
				_items[i] = new ItemHolder(objectId, count);
			}
		}

		_reqAdena = readQ();
	}

	@Override
	public void runImpl()
	{
		if(!Config.ALLOW_MAIL)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!Config.ALLOW_ATTACHMENTS)
		{
			_items = null;
			_isCod = false;
			_reqAdena = 0;
		}

		if(!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("У Вас недостаточно прав для выполнения этой операции.");
			return;
		}

		if(!activeChar.isInsideZone(ZONE_PEACE) && _items != null)
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_NOT_IN_PEACE_ZONE);
			return;
		}

		if(activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_DURING_EXCHANGE);
			return;
		}

		if(activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_DURING_ENCHANT);
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_PRIVATE_STORE);
			return;
		}

		if(_receiver.length() > MAX_RECV_LENGTH)
		{
			activeChar.sendPacket(SystemMessageId.ALLOWED_LENGTH_FOR_RECIPIENT_EXCEEDED);
			return;
		}

		if(_subject.length() > MAX_SUBJ_LENGTH)
		{
			activeChar.sendPacket(SystemMessageId.ALLOWED_LENGTH_FOR_TITLE_EXCEEDED);
			return;
		}

		if(_text.length() > MAX_TEXT_LENGTH)
		{
			// not found message for this
			activeChar.sendPacket(SystemMessageId.ALLOWED_LENGTH_FOR_TITLE_EXCEEDED);
			return;
		}

		if(_items != null && _items.length > MAX_ATTACHMENTS)
		{
			activeChar.sendPacket(SystemMessageId.ITEM_SELECTION_POSSIBLE_UP_TO_8);
			return;
		}

		if(_reqAdena < 0 || _reqAdena > MAX_ADENA)
		{
			return;
		}

		if(_isCod)
		{
			if(_reqAdena == 0)
			{
				activeChar.sendPacket(SystemMessageId.PAYMENT_AMOUNT_NOT_ENTERED);
				return;
			}
			if(_items == null || _items.length == 0)
			{
				activeChar.sendPacket(SystemMessageId.PAYMENT_REQUEST_NO_ITEM);
				return;
			}
		}

		int receiverId = CharNameTable.getInstance().getIdByName(_receiver);
		if(receiverId <= 0)
		{
			activeChar.sendPacket(SystemMessageId.RECIPIENT_NOT_EXIST);
			return;
		}

		if(receiverId == activeChar.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANT_SEND_MAIL_TO_YOURSELF);
			return;
		}

		L2AccessLevel accessLevel;
		int level = CharNameTable.getInstance().getAccessLevelById(receiverId);
		accessLevel = AdminTable.getInstance().getAccessLevel(level);

		if(accessLevel.isGm() && !activeChar.getAccessLevel().isGm())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_C1).addString(_receiver));
			return;
		}

		if(activeChar.isInJail() && (Config.JAIL_DISABLE_TRANSACTION && _items != null || Config.JAIL_DISABLE_CHAT))
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_NOT_IN_PEACE_ZONE);
			return;
		}

		if(RelationListManager.getInstance().isInBlockList(receiverId, activeChar.getObjectId()))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_BLOCKED_YOU_CANNOT_MAIL).addString(_receiver));
			return;
		}

		if(MailManager.getInstance().getOutboxSize(activeChar.getObjectId()) >= OUTBOX_SIZE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_MAIL_LIMIT_EXCEEDED);
			return;
		}

		if(MailManager.getInstance().getInboxSize(receiverId) >= INBOX_SIZE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_MAIL_LIMIT_EXCEEDED);
			return;
		}

		if(!getClient().getFloodProtectors().getSendMail().tryPerformAction(FloodAction.MAIL_SEND))
		{
			activeChar.sendPacket(SystemMessageId.CANT_FORWARD_LESS_THAN_MINUTE);
			return;
		}

		MailMessage msg = new MailMessage(activeChar.getObjectId(), receiverId, _isCod, _subject, _text, _reqAdena);
		if(removeItems(activeChar, msg))
		{
			MailManager.getInstance().sendMessage(msg);
			activeChar.sendPacket(ExReplyWritePost.valueOf(true));
			activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_SENT);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:66 RequestSendPost";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}

	private boolean removeItems(L2PcInstance player, MailMessage msg)
	{
		long currentAdena = player.getAdenaCount();
		long fee = MESSAGE_FEE;

		if(_items != null)
		{
			for(ItemHolder attachItem : _items)
			{
				// Check validity of requested item
				L2ItemInstance item = player.checkItemManipulation(attachItem.getId(), attachItem.getCount(), ProcessType.MAIL_ATTACH);
				if(item == null || !item.isTradeable() || item.isEquipped())
				{
					player.sendPacket(SystemMessageId.CANT_FORWARD_BAD_ITEM);
					return false;
				}

				fee += MESSAGE_FEE_PER_SLOT;

				if(item.getItemId() == ADENA_ID)
				{
					currentAdena -= attachItem.getCount();
				}
			}
		}

		// Check if enough adena and charge the fee
		if(currentAdena < fee || !player.reduceAdena(ProcessType.MAIL_UNKNOWN_1, fee, null, false))
		{
			player.sendPacket(SystemMessageId.CANT_FORWARD_NO_ADENA);
			return false;
		}

		if(_items == null)
		{
			return true;
		}

		Mail attachments = msg.createAttachments();

		// message already has attachments ? oO
		if(attachments == null)
		{
			return false;
		}

		StringBuilder recv = new StringBuilder(32);
		StringUtil.append(recv, msg.getReceiverName(), "[", String.valueOf(msg.getReceiverId()), "]");
		String receiver = recv.toString();

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for(ItemHolder attachItem : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(attachItem.getId(), attachItem.getCount(), ProcessType.MAIL_ATTACH);
			if(oldItem == null || !oldItem.isTradeable() || oldItem.isEquipped())
			{
				_log.log(Level.WARN, "Error adding attachment for char " + player.getName() + " (olditem == null)");
				return false;
			}

			L2ItemInstance newItem = player.getInventory().transferItem(ProcessType.MAIL_UNKNOWN_1, attachItem.getId(), attachItem.getCount(), attachments, player, receiver);
			if(newItem == null)
			{
				_log.log(Level.WARN, "Error adding attachment for char " + player.getName() + " (newitem == null)");
				continue;
			}
			newItem.setLocation(newItem.getItemLocation(), msg.getId());

			if(playerIU != null)
			{
				if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}

		// Send updated item list to the player
		if(playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}

		// Обновляем вес инвентаря
		player.sendPacket(new ExUserInfoInvenWeight(player));

		return true;
	}
}