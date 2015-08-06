package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.attribute.ExBaseAttributeCancelResult;

public class RequestExRemoveItemAttribute extends L2GameClientPacket
{
	private int _objectId;
	private long _price;
	private byte _element;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_element = (byte) readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_objectId);

		if(targetItem == null)
		{
			return;
		}

		if(targetItem.getElementals() == null || targetItem.getElemental(_element) == null)
		{
			return;
		}

		if(activeChar.reduceAdena(ProcessType.CONSUME, getPrice(targetItem), activeChar, true))
		{
			if(targetItem.isEquipped())
			{
				targetItem.getElemental(_element).removeBonus(activeChar);
			}
			targetItem.clearElementAttr(_element);
			activeChar.sendUserInfo();

			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(targetItem);
			activeChar.sendPacket(iu);
			SystemMessage sm;
			byte realElement = targetItem.isArmor() ? Elementals.getOppositeElement(_element) : _element;
			if(targetItem.getEnchantLevel() > 0)
			{
				sm = targetItem.isArmor() ? SystemMessage.getSystemMessage(SystemMessageId.S1_S2_S3_ATTRIBUTE_REMOVED_RESISTANCE_TO_S4_DECREASED) : SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ELEMENTAL_POWER_REMOVED);
				sm.addNumber(targetItem.getEnchantLevel());
				sm.addItemName(targetItem);
				sm.addElemental(realElement);
				if(targetItem.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			else
			{
				sm = targetItem.isArmor() ? SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ATTRIBUTE_REMOVED_RESISTANCE_S3_DECREASED) : SystemMessage.getSystemMessage(SystemMessageId.S1_ELEMENTAL_POWER_REMOVED);
				sm.addItemName(targetItem);
				sm.addElemental(realElement);
				if(targetItem.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			activeChar.sendPacket(sm);
			activeChar.sendPacket(new ExBaseAttributeCancelResult(targetItem.getObjectId(), _element));
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_FUNDS_TO_CANCEL_ATTRIBUTE);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:23 RequestExRemoveItemAttribute";
	}

	private long getPrice(L2ItemInstance item)
	{
		switch(item.getItem().getCrystalType())
		{
			case S:
				_price = item.isWeapon() ? 50000 : 40000;
				break;
			case S80:
				_price = item.isWeapon() ? 100000 : 80000;
				break;
			case S84:
				_price = item.isWeapon() ? 200000 : 160000;
				break;
			case R:
				_price = item.isWeapon() ? 400000 : 320000;
				break;
			case R95:
				_price = item.isWeapon() ? 800000 : 640000;
				break;
			case R99:
				_price = item.isWeapon() ? 3200000 : 2560000;
				break;
		}

		return _price;
	}
}