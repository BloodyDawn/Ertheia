package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;

	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		// Wear-items are not to be unequipped.
		if(item == null)
		{
			return;
		}

		if(!EventManager.onRequestUnEquipItem(activeChar))
		{
			return;
		}

		// The English system message say weapon, but it's applied to any equipped item.
		if(activeChar.isAttackingNow() || activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_CHANGE_WEAPON_DURING_AN_ATTACK);
			return;
		}

		// Arrows and bolts.
		if(_slot == L2Item.SLOT_L_HAND && item.getItem() instanceof L2EtcItem)
		{
			return;
		}

		// Prevent of unequipping a cursed weapon.
		if(_slot == L2Item.SLOT_LR_HAND && (activeChar.isCursedWeaponEquipped() || activeChar.isCombatFlagEquipped()))
		{
			return;
		}

		// Prevent player from unequipping items in special conditions.
		if(activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
		{
			return;
		}

		if(!activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
		{
			activeChar.sendPacket(SystemMessageId.ITEM_CANNOT_BE_TAKEN_OFF);
			return;
		}

		if(item.isWeapon() && item.getWeaponItem().isForceEquip() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.ITEM_CANNOT_BE_TAKEN_OFF);
			return;
		}

		L2ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		InventoryUpdate iu = new InventoryUpdate();
		for(L2ItemInstance itm : unequipped)
		{
			activeChar.checkSShotsMatch(null, itm);
			iu.addModifiedItem(itm);
		}

		// Show the update in the inventory.
		activeChar.sendPacket(iu);
		activeChar.broadcastUserInfo();

		// This can be 0 if the user pressed the right mouse button twice very fast.
		if(unequipped.length > 0)
		{
			SystemMessage sm = null;
			if(unequipped[0].getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequipped[0].getEnchantLevel());
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
			}
			sm.addItemName(unequipped[0]);
			activeChar.sendPacket(sm);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 16 RequestUnequipItem";
	}
}
