package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.CrystallizationData;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

public class RequestCrystallizeItem extends L2GameClientPacket
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
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			_log.log(Level.INFO, "RequestCrystalizeItem: activeChar was null");
			return;
		}

		if(_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if(skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getClassId().getId() < 138)
		{
			if(activeChar.getRace() != Race.Dwarf && activeChar.getClassId().ordinal() != 117 && activeChar.getClassId().ordinal() != 55)
			{
				_log.log(Level.INFO, "Player " + activeChar.getClient() + " used crystalize with classid: " + activeChar.getClassId().ordinal());
				return;
			}
		}

		PcInventory inventory = activeChar.getInventory();
		if(inventory != null)
		{
			L2ItemInstance item = inventory.getItemByObjectId(_objectId);
			if(item == null)
			{
				activeChar.sendActionFailed();
				return;
			}

			if(item.isHeroItem())
			{
				return;
			}

			if(_count > item.getCount())
			{
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
			}
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if(itemToRemove == null || itemToRemove.isShadowItem() || itemToRemove.isTimeLimitedItem())
		{
			return;
		}

		if(!itemToRemove.getItem().isCrystallizable() || itemToRemove.getItem().getCrystalCount() <= 0 || itemToRemove.getItem().getCrystalType() == CrystalGrade.NONE)
		{
			_log.log(Level.WARN, activeChar.getName() + " (" + activeChar.getObjectId() + ") tried to crystallize " + itemToRemove.getItem().getItemId());
			return;
		}

		if(!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId()))
		{
			activeChar.sendMessage("Cannot use this item.");
			return;
		}

		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;

		switch(itemToRemove.getItem().getItemGradeSPlus())
		{
			case C:
				if(skillLevel <= 1)
				{
					canCrystallize = false;
				}
				break;
			case B:
				if(skillLevel <= 2)
				{
					canCrystallize = false;
				}
				break;
			case A:
				if(skillLevel <= 3)
				{
					canCrystallize = false;
				}
				break;
			case S:
				if(skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			case R:
				if(skillLevel <= 5)
				{
					canCrystallize = false;
				}
				break;
		}

		if(!canCrystallize)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setInCrystallize(true);

		// Снимаем предмет, еслио он одет
		if(itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance item : unequiped)
			{
				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);

			if(itemToRemove.getEnchantLevel() > 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(itemToRemove.getEnchantLevel()).addItemName(itemToRemove));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(itemToRemove));
			}
		}

		// Удаляем предмет из инвентаря
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem(ProcessType.CRYSTALIZE, _objectId, _count, activeChar, null);

		InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		activeChar.sendPacket(iu);

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED).addItemName(removedItem));

		L2ItemInstance createditem;

		// Добавляем кристаллы предмета
		int crystalId = itemToRemove.getItem().getCrystalItemId();
		int crystalAmount = itemToRemove.getCrystalCount();
		activeChar.getInventory().addItem(ProcessType.CRYSTALIZE, crystalId, crystalAmount, activeChar, activeChar);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystalId).addItemNumber(crystalAmount));

		// получаем лист возможных продуктов
		if(CrystallizationData.getInstance().isItemExistInTable(removedItem))
		{
			CrystallizationData.getInstance().getProductsForItem(itemToRemove).stream().filter(item -> Rnd.getChance(item.getChance())).forEach(item -> {
				activeChar.getInventory().addItem(ProcessType.CRYSTALIZE, item.getId(), item.getCount(), activeChar, activeChar);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item.getId()).addItemNumber(item.getCount()));
			});
		}
		activeChar.broadcastUserInfo();
		WorldManager.getInstance().removeObject(removedItem);
		activeChar.setInCrystallize(false);
	}

	@Override
	public String getType()
	{
		return "[C] 72 RequestCrystallizeItem";
	}
}