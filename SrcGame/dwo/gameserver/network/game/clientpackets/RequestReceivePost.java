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
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
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
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExShowReceivedPostList;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExUnReadMailCount;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.actor.L2Character.ZONE_PEACE;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;

/**
 * @author Migi, DS
 */
public class RequestReceivePost extends L2GameClientPacket
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
		if(!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.MAIL_GET))
		{
			return;
		}

		if(!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disabled for your Access Level");
			return;
		}

		if(!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_NOT_IN_PEACE_ZONE);
			return;
		}

		if(activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_EXCHANGE);
			return;
		}

		if(activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_DURING_ENCHANT);
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_PRIVATE_STORE);
			return;
		}

		MailMessage msg = MailManager.getInstance().getMessage(_msgId);
		if(msg == null)
		{
			return;
		}

		if(msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get not own attachment!", Config.DEFAULT_PUNISH);
			return;
		}

		if(!msg.hasAttachments())
		{
			return;
		}

		ItemContainer attachments = msg.getAttachments();
		if(attachments == null)
		{
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

			// Calculate needed slots
			if(item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get wrong item (ownerId != senderId) from attachment!", Config.DEFAULT_PUNISH);
				return;
			}

			if(item.getItemLocation() != ItemLocation.MAIL)
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get wrong item (Location != MAIL) from attachment!", Config.DEFAULT_PUNISH);
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

		// Item Max Limit Check
		if(!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL);
			return;
		}

		// Weight limit Check
		if(!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL);
			return;
		}

		long adena = msg.getReqAdena();
		if(adena > 0 && !activeChar.reduceAdena(ProcessType.MAIL_UNKNOWN_1, adena, null, true))
		{
			activeChar.sendPacket(SystemMessageId.CANT_RECEIVE_NO_ADENA);
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

			if(item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items with owner != sender !", Config.DEFAULT_PUNISH);
				return;
			}

			long count = item.getCount();
			L2ItemInstance newItem = attachments.transferItem(ProcessType.MAIL_UNKNOWN_1, item.getObjectId(), item.getCount(), activeChar.getInventory(), activeChar, null);
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

		// Send updated item list to the player
		if(playerIU != null)
		{
			activeChar.sendPacket(playerIU);
		}
		else
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}

		msg.removeAttachments();

		// Update current load status on player
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));

		L2PcInstance sender = WorldManager.getInstance().getPlayer(msg.getSenderId());
		if(adena > 0)
		{
			if(sender != null)
			{
				sender.addAdena(ProcessType.MAIL_UNKNOWN_1, adena, activeChar, false);
				sender.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2).addItemNumber(adena).addCharName(activeChar));
			}
			else
			{
				L2ItemInstance paidAdena = ItemTable.getInstance().createItem(ProcessType.MAIL_UNKNOWN_1, ADENA_ID, adena, activeChar, null);
				paidAdena.setOwnerId(msg.getSenderId());
				paidAdena.setLocation(ItemLocation.INVENTORY);
				paidAdena.updateDatabase(true);
				WorldManager.getInstance().removeObject(paidAdena);
			}
		}
		else if(sender != null)
		{
			sender.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ACQUIRED_ATTACHED_ITEM).addCharName(activeChar));
		}

		activeChar.sendPacket(new ExChangePostState(true, _msgId, MailMessageStatus.READED));
		activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RECEIVED);
		activeChar.sendPacket(new ExShowReceivedPostList(activeChar.getObjectId()));
		activeChar.sendPacket(new ExUnReadMailCount(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] D0:6A RequestPostAttachment";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}

}