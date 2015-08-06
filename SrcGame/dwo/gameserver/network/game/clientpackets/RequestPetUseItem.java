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

import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.handler.ItemHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetItemList;
import org.apache.log4j.Level;

public class RequestPetUseItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		// todo implement me properly
		//readQ();
		//readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2PetInstance pet = (L2PetInstance) activeChar.getItemPet();
		if(pet == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getUseItem().tryPerformAction(FloodAction.PET_USE_ITEM))
		{
			return;
		}

		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			return;
		}

		if(activeChar.isAlikeDead() || pet.isDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return;
		}

		if(!item.isEquipped())
		{
			if(!item.getItem().checkCondition(item, pet, pet, true))
			{
				return;
			}
		}

		//check if the item matches the pet
		if(item.isEquipable())
		{
			// all pet items have condition
			if(!item.getItem().isConditionAttached())
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			useItem(pet, item, activeChar);
			return;
		}
		int itemId = item.getItemId();
		if(PetDataTable.isPetFood(itemId))
		{
			if(pet.canEatFoodId(itemId))
			{
				useItem(pet, item, activeChar);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
		}

		IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
		if(handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 8A RequestPetUseItem";
	}

	private void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if(item.isEquipable())
		{
			if(item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
			else
			{
				pet.getInventory().equipItem(item);
			}

			activeChar.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if(handler != null)
			{
				handler.useItem(pet, item, false);
				pet.updateAndBroadcastStatus(1);
			}
			else
			{
				_log.log(Level.WARN, "No itemhandler registered for itemId:" + item.getItemId());
			}
		}
	}
}
