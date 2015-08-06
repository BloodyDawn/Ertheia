package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.model.player.mail.MailMessageStatus;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExChangePostState;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExUnReadMailCount;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.actor.L2Character.ZONE_PEACE;

public class RequestCancelSentPost extends L2GameClientPacket
{
	private int _msgId;

	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || !Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.MAIL_CANCEL))
		{
			return;
		}

		MailMessage msg = MailManager.getInstance().getMessage(_msgId);
		if(msg == null)
		{
			return;
		}
		if(msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to cancel not own post!", Config.DEFAULT_PUNISH);
			return;
		}

		if(!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_NOT_IN_PEACE_ZONE);
			return;
		}

		if(activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_DURING_EXCHANGE);
			return;
		}

		if(activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_DURING_ENCHANT);
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_PRIVATE_STORE);
			return;
		}

		if(!msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL);
			return;
		}

		ItemContainer attachments = msg.getAttachments();
		if(attachments == null || attachments.getSize() == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL);
			return;
		}

		int weight = 0;
		int slots = 0;

		for(L2ItemInstance item : attachments.getItems())
		{
			if(item == null)
			{
				continue;
			}

			if(item.getOwnerId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get not own item from cancelled attachment!", Config.DEFAULT_PUNISH);
				return;
			}

			if(item.getItemLocation() != ItemLocation.MAIL)
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items not from mail !", Config.DEFAULT_PUNISH);
				return;
			}

			if(item.getLocationSlot() != msg.getId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items from different attachment!", Config.DEFAULT_PUNISH);
				return;
			}

			weight += item.getCount() * item.getItem().getWeight();
			if(!item.isStackable())
			{
				slots += item.getCount();
			}
			else if(activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		if(!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_INVENTORY_FULL);
			return;
		}

		if(!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_INVENTORY_FULL);
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for(L2ItemInstance item : attachments.getItems())
		{
			if(item == null)
			{
				continue;
			}

			long count = item.getCount();
			L2ItemInstance newItem = attachments.transferItem(ProcessType.MAIL_UNKNOWN_1, item.getObjectId(), count, activeChar.getInventory(), activeChar, null);
			if(newItem == null)
			{
				return;
			}

			if(playerIU != null)
			{
				if(newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
			}
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ACQUIRED_S2_S1).addItemName(item.getItemId()).addItemNumber(count));
		}

		msg.removeAttachments();

		// Send updated item list to the player
		if(playerIU != null)
		{
			activeChar.sendPacket(playerIU);
		}
		else
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		L2PcInstance receiver = WorldManager.getInstance().getPlayer(msg.getReceiverId());
		if(receiver != null)
		{
			receiver.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELLED_MAIL).addCharName(activeChar));
			receiver.sendPacket(new ExChangePostState(true, _msgId, MailMessageStatus.DELETED));
			receiver.sendPacket(new ExUnReadMailCount(receiver));
		}

		MailManager.getInstance().deleteMessageInDb(_msgId);

		activeChar.sendPacket(new ExChangePostState(false, _msgId, MailMessageStatus.DELETED));
		activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_CANCELLED);
	}

	@Override
	public String getType()
	{
		return "[C] D0:6F RequestCancelPostAttachment";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}