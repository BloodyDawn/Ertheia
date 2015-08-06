package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.attribute.ExAttributeEnchantResult;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

public class RequestExEnchantItemAttribute extends L2GameClientPacket
{
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_objectId == 0xFFFFFFFF)
		{
			// Player canceled enchant
			player.setActiveEnchantAttrItem(null);
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			return;
		}

		if(!player.isOnline())
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}

		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP);
			player.setActiveEnchantAttrItem(null);
			return;
		}

		// Restrict enchant during a trade (bug if enchant fails)
		if(player.getActiveRequester() != null)
		{
			// Cancel trade
			player.cancelActiveTrade();
			player.setActiveEnchantAttrItem(null);
			player.sendMessage("Enchanting items is not allowed during a trade.");
			return;
		}

		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance stone = player.getActiveEnchantAttrItem();
		if(item == null || stone == null)
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}

		if(_count > stone.getCount())
		{
			return;
		}

		switch(item.getItemLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
				if(item.getOwnerId() != player.getObjectId())
				{
					player.setActiveEnchantAttrItem(null);
					return;
				}
				break;
			default:
				player.setActiveEnchantAttrItem(null);
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
				return;
		}

		if(!item.isElementable() || item.getItemType() == L2ArmorType.SHIELD || item.getItemType() == L2ArmorType.SIGIL) // XXX: UnAfraid: temp fix for shield / sigils untill they are fixed in dp!
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
			player.setActiveEnchantAttrItem(null);
			return;
		}

		int stoneId = stone.getItemId();
		byte elementToAdd = Elementals.getItemElement(stoneId);
		// Armors have the opposite element
		if(item.isArmor())
		{
			elementToAdd = Elementals.getOppositeElement(elementToAdd);
		}
		byte opositeElement = Elementals.getOppositeElement(elementToAdd);

		byte realElement = item.isArmor() ? opositeElement : elementToAdd;

		Elementals oldElement = item.getElemental(elementToAdd);
		int elementValue = oldElement == null ? 0 : oldElement.getValue();

		int limit = getLimit(item, stoneId);

		if(item.isWeapon() && oldElement != null && oldElement.getElement() != elementToAdd && oldElement.getElement() != -2 || item.isArmor() && item.getElemental(elementToAdd) == null && item.getElementals() != null && item.getElementals().length >= 3)
		{
			player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
			player.setActiveEnchantAttrItem(null);
			return;
		}

		if(item.isArmor() && item.getElementals() != null)
		{
			//cant add opposite element
			for(Elementals elm : item.getElementals())
			{
				if(elm.getElement() == opositeElement)
				{
					player.setActiveEnchantAttrItem(null);
					//Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to add oposite attribute to item!", Config.DEFAULT_PUNISH);
					return;
				}
			}
		}

		int newPower = elementValue;

		boolean success = false;
		int count = 0;
		int successCount = 0;
		for(int i = 0; i < _count; i++)
		{
			boolean success_arr = false;
			switch(Elementals.getItemElemental(stoneId)._type)
			{
				case Crystal:
					success_arr = Rnd.getChance(Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL);
					break;
				case Jewel:
					success_arr = Rnd.getChance(Config.ENCHANT_CHANCE_ELEMENT_JEWEL);
					break;
				case Energy:
					success_arr = Rnd.getChance(Config.ENCHANT_CHANCE_ELEMENT_ENERGY);
					break;
				case Roughore:
				case Stone:
				case StoneSuper:
				case StoneSuper60:
				case CrystalSuper:
				case CrystalSuper3to6:
					success_arr = true;
					break;
			}

			count++;
			if(success_arr)
			{
				success = true;
				successCount++;
				newPower += getPowerToAdd(stoneId, newPower, item, limit);
				if(newPower >= limit)
				{
					newPower = limit;
					if(elementValue >= newPower && successCount == 0)
					{
						player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
						//player.setActiveEnchantAttrItem(null);
						return;
					}
					break;
				}
			}
		}

		if(!player.destroyItem(ProcessType.NPC, stone, count, player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to attribute enchant with a stone he doesn't have", Config.DEFAULT_PUNISH);
			player.setActiveEnchantAttrItem(null);
			return;
		}

		int result;
		if(success)
		{
			SystemMessage sm;
			if(item.getEnchantLevel() == 0)
			{
				sm = item.isArmor() ? SystemMessage.getSystemMessage(SystemMessageId.THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED) : SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1);
				sm.addItemName(item);
				sm.addElemental(realElement);
				if(item.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			else
			{
				sm = item.isArmor() ? SystemMessage.getSystemMessage(SystemMessageId.THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED) : SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
				sm.addElemental(realElement);
				if(item.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			player.sendPacket(sm);

			item.setElementAttr(elementToAdd, newPower);
			if(item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}

			// send packets
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
			result = 0;
		}
		else
		{
			result = 2;
			player.sendPacket(SystemMessageId.FAILED_ADDING_ELEMENTAL_POWER);
		}
		player.sendPacket(new ExAttributeEnchantResult(result, item.isWeapon(), realElement, elementValue, newPower, successCount));
		player.sendUserInfo();
		player.setActiveEnchantAttrItem(null);
	}

	@Override
	public String getType()
	{
		return "[C] D0:35 RequestExEnchantItemAttribute";
	}

	public int getLimit(L2ItemInstance item, int stoneId)
	{
		Elementals.ElementalItems elementItem = Elementals.getItemElemental(stoneId);
		if(elementItem == null)
		{
			return 0;
		}

		// TODO: Move to the right place for validation .
		return item.isWeapon() ? Elementals.WEAPON_VALUES[getMax(elementItem._type._maxLevel, Config.ELEMENTAL_LEVEL_WEAPON)] : Elementals.ARMOR_VALUES[getMax(elementItem._type._maxLevel, Config.ELEMENTAL_LEVEL_ARMOR)];
	}

	private int getMax(int elementalValue, int limitValue)
	{
		if(Config.ELEMENTAL_CUSTOM_LEVEL_ENABLE)
		{
			if(limitValue >= elementalValue)
			{
				return elementalValue;
			}
			else if(limitValue <= elementalValue)
			{
				return limitValue;
			}
		}
		return elementalValue;
	}

	public int getPowerToAdd(int stoneId, int oldValue, L2ItemInstance item, int limit)
	{
		if(Elementals.getItemElement(stoneId) != Elementals.NONE)
		{
			switch(Elementals.getItemElemental(stoneId)._type)
			{
				case Stone:
				case Roughore:
				case Crystal:
				case Energy:
					if(item.isWeapon())
					{
						return oldValue == 0 ? Elementals.FIRST_WEAPON_BONUS : Elementals.NEXT_WEAPON_BONUS;
					}
					if(item.isArmor())
					{
						return Elementals.ARMOR_BONUS;
					}
					break;
				case StoneSuper:
				case StoneSuper60:
				case CrystalSuper:
				{
					int value = limit - oldValue;
					if(value < 0)
					{
						value = 0;
					}
					if(item.isWeapon())
					{
						return value;
					}
					else if(item.isArmor())
					{
						return value;
					}
				}
				break;
				case CrystalSuper3to6:
					int value = limit - oldValue;

					if(value < 0)
					{
						value = 0;
					}

					if(item.isWeapon())
					{
						if(oldValue >= value && oldValue <= value)
						{
							return value;
						}
					}
					else if(item.isArmor())
					{
						if(oldValue >= value && oldValue <= value)
						{
							return value;
						}
					}
					break;
			}
		}
		return 0;
	}
}