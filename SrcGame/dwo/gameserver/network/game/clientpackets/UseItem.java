package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.handler.ItemHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.NextAction;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExUseSharedGroupItem;

public class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	private int _itemId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		// Flood protect UseItem
		if(!getClient().getFloodProtectors().getUseItem().tryPerformAction(FloodAction.USE_ITEM))
		{
			return;
		}

		if(activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			activeChar.sendActionFailed();
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(item == null)
		{
			return;
		}

		if(item.getItem().getType2() == L2Item.TYPE2_QUEST && (!item.isEtcItem() || item.getEtcItem().getHandlerName() == null))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}

		// No UseItem is allowed while the player is in special conditions
		if(activeChar.isStunned() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isAfraid() || activeChar.isAlikeDead() || activeChar.isFlyUp() || activeChar.isKnockBacked())
		{
			return;
		}

		// Char cannot use item when dead
		if(activeChar.isDead())
		{
			getClient().getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return;
		}

		if(!item.isEquipped() && !item.getItem().checkCondition(item, activeChar, activeChar, true))
		{
			return;
		}

		_itemId = item.getItemId();
		if(!activeChar.getInventory().canManipulateWithItemId(_itemId))
		{
			activeChar.sendMessage("Сейчас нельзя использовать этот предмет.");
			return;
		}

		if(activeChar.isFishing() && (_itemId < 6535 || _itemId > 6540))
		{
			// You cannot do anything else while fishing
			getClient().getActiveChar().sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		if(activeChar.hasBadReputation())
		{
			SkillHolder[] sHolders = item.getItem().getSkills();
			if(sHolders != null)
			{
				for(SkillHolder sHolder : sHolders)
				{
					L2Skill skill = sHolder.getSkill();
					if(skill != null && (skill.getSkillType() == L2SkillType.TELEPORT || skill.getSkillType() == L2SkillType.RECALL))
					{
						return;
					}
				}
			}
		}

		// If the item has reuse time and it has not passed.
		// Message from reuse delay must come from item.
		int reuseDelay = item.getReuseDelay();
		int sharedReuseGroup = item.getSharedReuseGroup();
		if(reuseDelay > 0)
		{
			long reuse = activeChar.getItemRemainingReuseTime(item.getObjectId());
			if(reuse > 0)
			{
				reuseData(activeChar, item);
				sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuse, reuseDelay);
				return;
			}

			long reuseOnGroup = activeChar.getReuseDelayOnGroup(sharedReuseGroup);
			if(reuseOnGroup > 0)
			{
				reuseData(activeChar, item);
				sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseOnGroup, reuseDelay);
				return;
			}
		}

		if(item.isEquipable())
		{
			if(activeChar.isCursedWeaponEquipped() && _itemId == 6408) // Don't allow to put formal wear
			{
				return;
			}

			// Equip or unEquip
			if(!EventManager.onUseItem(activeChar))
			{
				return;
			}
            if (item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND || item.getItem().getBodyPart() == L2Item.SLOT_L_HAND || item.getItem().getBodyPart() == L2Item.SLOT_R_HAND) {// prevent players to equip weapon while wearing combat flag
                if (activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemId() == 9819) {
                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                    return;
                }
                if (activeChar.isMounted()) {
                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                    return;
                }
                if (activeChar.isDisarmed()) {
                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                    return;
                }

                // Don't allow weapon/shield equipment if a cursed weapon is equiped
                if (activeChar.isCursedWeaponEquipped()) {
                    return;
                }

                // Don't allow other Race to Wear Kamael exclusive Weapons.
                if (!item.isEquipped() && item.getItem() instanceof L2Weapon && !activeChar.isGM()) {
                    L2Weapon wpn = (L2Weapon) item.getItem();

                    switch (activeChar.getRace()) {
                        case Kamael:
                            switch (wpn.getItemType()) {
                                case NONE:
                                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                    return;
                            }
                            break;
                        case Human:
                        case Dwarf:
                        case Elf:
                        case DarkElf:
                        case Orc:
                            switch (wpn.getItemType()) {
                                case RAPIER:
                                case CROSSBOW:
                                case ANCIENTSWORD:
                                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                    return;
                            }
                            break;
                        case Ertheia:
                            switch (wpn.getItemType()) {
                                case SWORD:
                                case DAGGER:
                                case BOW:
                                case POLE:
                                case NONE:
                                case DUAL:
                                case RAPIER:
                                case ANCIENTSWORD:
                                case CROSSBOW:
                                case DUALDAGGER:
                                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                                    return;
                            }
                            break;
                    }
                }

            } else if (item.getItem().getBodyPart() == L2Item.SLOT_CHEST || item.getItem().getBodyPart() == L2Item.SLOT_BACK || item.getItem().getBodyPart() == L2Item.SLOT_GLOVES || item.getItem().getBodyPart() == L2Item.SLOT_FEET || item.getItem().getBodyPart() == L2Item.SLOT_HEAD || item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR || item.getItem().getBodyPart() == L2Item.SLOT_LEGS) {
                if (activeChar.getFirstEffect(462) != null && (item.getItem().getItemType() == L2ArmorType.HEAVY || item.getItem().getItemType() == L2ArmorType.MAGIC)) {
                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                    return;
                }

            } else if (item.getItem().getBodyPart() == L2Item.SLOT_DECO) {
                if (!item.isEquipped() && activeChar.getInventory().getMaxTalismanCount() == 0) {
                    activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                    return;
                }
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_BROACH) {
                if (!item.isEquipped() && activeChar.getInventory().getMaxStoneCount() == 0) {
                    activeChar.sendPacket(SystemMessageId.getSystemMessageId(4237));
                    return;
                }
            }

			if(activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			{
				// Creating next action class.
				NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, () -> activeChar.useEquippableItem(item, true));

				// Binding next action to AI.
				activeChar.getAI().setNextAction(nextAction);
			}
			else if(activeChar.isAttackingNow())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new WeaponEquipTask(item, activeChar), (activeChar.getAttackEndTime() - GameTimeController.getInstance().getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
			}
			else
			{
				activeChar.useEquippableItem(item, true);
			}
		}
		else
		{
			if(activeChar.isCastingNow() && !(item.isPotion() || item.isElixir()))
			{
				return;
			}

			L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			if(weaponItem != null && weaponItem.getItemType() == L2WeaponType.FISHINGROD && (_itemId >= 6519 && _itemId <= 6527 || _itemId >= 7610 && _itemId <= 7613 || _itemId >= 7807 && _itemId <= 7809 || _itemId >= 8484 && _itemId <= 8486 || _itemId >= 8505 && _itemId <= 8513))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				sendPacket(new ItemList(activeChar, false));
				return;
			}
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());

			if(handler != null && handler.useItem(activeChar, item, _ctrlPressed))
			{
				if(reuseDelay > 0)
				{
					activeChar.addTimeStampItem(item, reuseDelay);
					sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseDelay, reuseDelay);
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 14 UseItem";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return !Config.SPAWN_PROTECTION_ALLOWED_ITEMS.contains(_itemId);
	}

	private void reuseData(L2PcInstance activeChar, L2ItemInstance item)
	{
		SystemMessage sm = null;
		long remainingTime = activeChar.getItemRemainingReuseTime(item.getObjectId());
		long reuseOnGroup = activeChar.getReuseDelayOnGroup(item.getSharedReuseGroup());
		if(remainingTime <= 0 && reuseOnGroup > 0)
		{
			remainingTime = reuseOnGroup;
		}
		int hours = (int) (remainingTime / 3600000L);
		int minutes = (int) (remainingTime % 3600000L) / 60000;
		int seconds = (int) (remainingTime / 1000 % 60);
		if(hours > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
			sm.addNumber(hours);
			sm.addNumber(minutes);
		}
		else if(minutes > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
			sm.addNumber(minutes);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
		}
		sm.addNumber(seconds);
		activeChar.sendPacket(sm);
	}

	private void sendSharedGroupUpdate(L2PcInstance activeChar, int group, long remaining, int reuse)
	{
		if(group > 0)
		{
			activeChar.sendPacket(new ExUseSharedGroupItem(_itemId, group, remaining, reuse));
		}
	}

	public static class WeaponEquipTask implements Runnable
	{
		L2ItemInstance item;
		L2PcInstance activeChar;

		public WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}

		@Override
		public void run()
		{
			//If character is still engaged in strike we should not change weapon
			if(activeChar.isAttackingNow())
			{
				return;
			}
			// Equip or unEquip
			activeChar.useEquippableItem(item, false);
		}
	}
}
