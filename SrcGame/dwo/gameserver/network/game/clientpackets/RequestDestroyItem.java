package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

public class RequestDestroyItem extends L2GameClientPacket
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
			return;
		}

		if(_count <= 0)
		{
			if(_count < 0)
			{
				Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to destroy item with oid " + _objectId + " but has count < 0!", Config.DEFAULT_PUNISH);
			}
			return;
		}

		long count = _count;

		if(activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		// if we can't find the requested item, its actually a cheat
		if(itemToRemove == null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}

		// Cannot discard item that the skill is consuming
		if(activeChar.isCastingNow())
		{
			if(activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}
		// Cannot discard item that the skill is consuming
		if(activeChar.isCastingSimultaneouslyNow())
		{
			if(activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}

		int itemId = itemToRemove.getItemId();

		if(!activeChar.isGM() && !itemToRemove.isDestroyable() || CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			if(itemToRemove.isHeroItem())
			{
				activeChar.sendPacket(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			}
			return;
		}

		if(!itemToRemove.isStackable() && count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to destroy a non-stackable item with oid " + _objectId + " but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}

		if(!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId()))
		{
			activeChar.sendMessage("Нельзя использовать этот предмет.");
			return;
		}

		if(_count > itemToRemove.getCount())
		{
			count = itemToRemove.getCount();
		}

		if(itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance item : unequiped)
			{
				activeChar.checkSShotsMatch(null, item);
				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		if(PetDataTable.isPetItem(itemId))
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				if(!activeChar.getPets().isEmpty())
				{
					activeChar.getPets().stream().filter(pet -> pet.getControlObjectId() == _objectId).forEach(pet -> pet.getLocationController().decay());
				}

				// if it's a pet control item, delete the pet
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "could not delete pet objectid: ", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
		if(itemToRemove.isTimeLimitedItem())
		{
			itemToRemove.endOfLife();
		}

		if(itemToRemove.isEquipped())
		{
			if(itemToRemove.getEnchantLevel() > 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(itemToRemove.getEnchantLevel());
				sm.addItemName(itemToRemove);
				activeChar.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(itemToRemove);
				activeChar.sendPacket(sm);
			}

			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());

			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			activeChar.sendPacket(iu);
		}

		L2ItemInstance removedItem = activeChar.getInventory().destroyItem(ProcessType.DESTROY, _objectId, count, activeChar, null);

		if(removedItem == null)
		{
			return;
		}

		if(Config.FORCE_INVENTORY_UPDATE)
		{
			sendPacket(new ItemList(activeChar, true));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
			if(removedItem.getCount() == 0)
			{
				iu.addRemovedItem(removedItem);
			}
			else
			{
				iu.addModifiedItem(removedItem);
			}
			activeChar.sendPacket(iu);
		}

		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] 59 RequestDestroyItem";
	}
}