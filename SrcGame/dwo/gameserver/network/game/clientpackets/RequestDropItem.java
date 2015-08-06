package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.util.GMAudit;
import dwo.gameserver.util.Util;

public class RequestDropItem extends L2GameClientPacket
{
	private int _objectId;
	private long _count;
	private int _x;
	private int _y;
	private int _z;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_x = readD();
		_y = readD();
		_z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isDead())
		{
			return;
		}
		if(!getClient().getFloodProtectors().getDropItem().tryPerformAction(FloodAction.DROP_ITEM))
		{
			return;
		}
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(item == null || _count == 0 || !activeChar.validateItemManipulation(_objectId, "drop") || !Config.ALLOW_DISCARDITEM && !activeChar.isGM() || item.isAugmented() || !item.isDropable() && !(activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS) || item.getItemType() == L2EtcItemType.PET_COLLAR && activeChar.havePetInvItems() || activeChar.isInsideZone(L2Character.ZONE_NOITEMDROP))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if(item.isQuestItem() && !(activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS))
		{
			return;
		}

		if(_count > item.getCount())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}

		if(Config.PLAYER_SPAWN_PROTECTION > 0 && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}

		if(_count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to drop item with oid " + _objectId + " but has count < 0!", Config.DEFAULT_PUNISH);
			return;
		}

		if(!item.isStackable() && _count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to drop non-stackable item with oid " + _objectId + " but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}

		if(Config.JAIL_DISABLE_TRANSACTION && activeChar.isInJail())
		{
			activeChar.sendMessage("В Тюрьме нельзя мусорить.");
			return;
		}

		if(!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Недостаточно прав для выполнения запроса.");
			activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}

		if(activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		if(activeChar.isFishing())
		{
			//You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
			return;
		}
		if(activeChar.isFlying())
		{
			return;
		}

		// Cannot discard item that the skill is consuming
		if(activeChar.isCastingNow())
		{
			if(activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}

		// Cannot discard item that the skill is consuming
		if(activeChar.isCastingSimultaneouslyNow())
		{
			if(activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}

		if(item.getItem().getType2() == L2Item.TYPE2_QUEST && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
			return;
		}

		if(!activeChar.isInsideRadius(_x, _y, 150, false) || Math.abs(_z - activeChar.getZ()) > 50)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
			return;
		}

		if(!activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
		{
			activeChar.sendMessage("Вы не можете использовать этот предмет.");
			return;
		}

		if(item.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance itm : unequiped)
			{
				activeChar.checkSShotsMatch(null, itm);

				iu.addModifiedItem(itm);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();

			ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}

		L2ItemInstance dropedItem = activeChar.dropItem(ProcessType.DROP, _objectId, _count, _x, _y, _z, null, false, false);

		if(activeChar.isGM())
		{
			String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + ']', "Drop", target, "(id: " + dropedItem.getItemId() + " name: " + dropedItem.getItemName() + " objId: " + dropedItem.getObjectId() + " x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ() + ')');
		}
	}

	@Override
	public String getType()
	{
		return "[C] 12 RequestDropItem";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}