package dwo.gameserver.network.game.clientpackets.packet.enchant.item;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.datatables.xml.EnchantItemData;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.logengine.formatters.EnchantLogFormatter;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.EnchantItem;
import dwo.gameserver.model.items.EnchantScroll;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.enchant.item.EnchantResult;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RequestEnchantItem extends L2GameClientPacket
{
	protected static final Logger _logEnchant = LogManager.getLogger("enchantItem");

	private int _objectId;
	private int _supportId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_supportId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null || _objectId == 0)
		{
			return;
		}

		if(!activeChar.isOnline() || getClient().isDetached())
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}

		if(activeChar.isProcessingTransaction() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
			activeChar.setActiveEnchantItem(null);
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();
		L2ItemInstance support = activeChar.getActiveEnchantSupportItem();

		if(item == null || scroll == null)
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}

		// Обьект-шаблон для свитка заточки
		EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);

		// Свиток не найден в таблице свитков-заточек
		if(scrollTemplate == null)
		{
			return;
		}

		// Обьект-шаблон для усилителя заточки, если таковой имеется
		EnchantItem supportTemplate = null;
		if(support != null)
		{
			if(support.getObjectId() != _supportId)
			{
				activeChar.setActiveEnchantItem(null);
				return;
			}
			supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
		}

		// Первая проверка валиднсти текущей заточки
		if(!scrollTemplate.isValid(item, supportTemplate) || !item.isEnchantable())
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}

		// Проверка на программы-автоенчантеры
		if(activeChar.getActiveEnchantTimestamp() == 0 || System.currentTimeMillis() - activeChar.getActiveEnchantTimestamp() < 1500)
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " use autoenchant program ", Config.DEFAULT_PUNISH);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}

		// Попытка удалить свиток заточки
		scroll = activeChar.getInventory().destroyItem(ProcessType.ENCHANT, scroll.getObjectId(), 1, activeChar, item);
		if(scroll == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesn't have", Config.DEFAULT_PUNISH);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}

		// Попытка удалить усилитель, если таковой существует
		if(support != null)
		{
			support = activeChar.getInventory().destroyItem(ProcessType.ENCHANT, support.getObjectId(), 1, activeChar, item);
			if(support == null)
			{
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a support item he doesn't have", Config.DEFAULT_PUNISH);
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
				return;
			}
		}

		synchronized(item)
		{
            int minLuc = activeChar.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getLuc();
            int maxLuc = activeChar.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getLuc();
            int luc = Math.min(maxLuc, Math.max(minLuc, activeChar.getBaseTemplate().getBaseLUC() + activeChar.getHennaStatLUC()));
			double chance = scrollTemplate.getChance(item, supportTemplate) * Rnd.get(luc);

			L2Item it = item.getItem();

			// Последняя проверка валидности точки
			if(item.getOwnerId() != activeChar.getObjectId() || !item.isEnchantable() || chance < 0)
			{
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
				return;
			}

			boolean success = Rnd.getChance(chance);

			if(success)
			{
				// success
				item.setEnchantLevel(item.getEnchantLevel() + 1);
				item.updateDatabase();
				activeChar.sendPacket(new EnchantResult(EnchantResult.SUCCESS, item));

				if(Config.LOG_ITEM_ENCHANTS)
				{
					_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Success", new Object[]{
						activeChar, item, scroll, support, chance
					}));
				}

				// Анносируем успешную заточку предмета
				int minEnchantAnnounce = item.isArmor() ? 6 : 7;
				int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
				if(item.getEnchantLevel() == minEnchantAnnounce || item.getEnchantLevel() == maxEnchantAnnounce)
				{
					activeChar.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_SUCCESSFULY_ENCHANTED_A_S2_S3).addCharName(activeChar).addNumber(item.getEnchantLevel()).addItemName(item));

					L2Skill skill = SkillTable.FrequentSkill.FIREWORK.getSkill();
					if(skill != null)
					{
						activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
					}
				}

				if(it instanceof L2Armor)
				{
					// Если заточили броню на +4 добавляем скилл-бонус
					FastMap<Integer, SkillHolder> enchant4Skill = ((L2Armor) it).getEnchantSkills();
					if(enchant4Skill != null && enchant4Skill.containsKey(item.getEnchantLevel()) && activeChar.getInventory().getItemByObjectId(item.getObjectId()).isEquipped())
					{
                        activeChar.addSkill(enchant4Skill.get(item.getEnchantLevel()).getSkill(), false);
                        activeChar.sendSkillList();
                    }

					// Обновляем статистику заточки брони для игрока
					if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
					{
						activeChar.updateWorldStatistic(CategoryType.ARMOR_ENCHANT_TRY, item.getItem().getItemGrade(), item.getEnchantLevel());
					}
				}
				else if(it instanceof L2Weapon)
				{
					// Обновляем статистику заточки оружия для игрока
					if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
					{
						activeChar.updateWorldStatistic(CategoryType.WEAPON_ENCHANT_TRY, item.getItem().getItemGrade(), item.getEnchantLevel());
					}
				}
			}
			else
			{
				if(scrollTemplate.isSafe())
				{
					// Безопасная заточка - оставляем точку предмета такой, какая была до заточки
					activeChar.sendPacket(new EnchantResult(EnchantResult.SAFE_FAIL, item));

					if(Config.LOG_ITEM_ENCHANTS)
					{
						_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Success", new Object[]{
							activeChar, item, scroll, support, chance
						}));
					}
				}
				else
				{
					// Снимаем предмет с игрока в случае неудачной заточке
					if(item.isEquipped())
					{
						if(item.getEnchantLevel() > 0)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));
						}

						L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
						InventoryUpdate iu = new InventoryUpdate();
						for(L2ItemInstance itm : unequiped)
						{
							iu.addModifiedItem(itm);
						}

						activeChar.sendPacket(iu);
						activeChar.broadcastUserInfo();
					}

					if(scrollTemplate.isBlessed())
					{
						// Благославленная заточка - сбрасываем заточку на 0
						activeChar.sendPacket(SystemMessageId.BLESSED_ENCHANT_FAILED);

						item.setEnchantLevel(0);
						item.updateDatabase();
						activeChar.sendPacket(new EnchantResult(EnchantResult.BLESSED_FAIL, 0, 0));

						if(Config.LOG_ITEM_ENCHANTS)
						{
							_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Success", new Object[]{
								activeChar, item, scroll, support, chance
							}));
						}
					}
					else
					{
						// Заточка неудачна - удаляем предмет
						int crystalId = item.getItem().getCrystalItemId();
						int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
						if(count < 1)
						{
							count = 1;
						}

						L2ItemInstance destroyItem = activeChar.getInventory().destroyItem(ProcessType.ENCHANT, item, activeChar, null);
						if(destroyItem == null)
						{
							// Если попытка удаления неудачна, скорее всего используется фейк-предмет и игрок читер
							Util.handleIllegalPlayerAction(activeChar, "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !", Config.DEFAULT_PUNISH);
							activeChar.setActiveEnchantItem(null);
							activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));

							if(Config.LOG_ITEM_ENCHANTS)
							{
								_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Unable to destroy", new Object[]{
									activeChar, item, scroll, support, chance
								}));
							}
							return;
						}

						L2ItemInstance crystals = null;
						if(crystalId != 0)
						{
							crystals = activeChar.getInventory().addItem(ProcessType.ENCHANT, crystalId, count, activeChar, destroyItem);
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals).addItemNumber(count));
						}

						if(Config.FORCE_INVENTORY_UPDATE)
						{
							activeChar.sendPacket(new ItemList(activeChar, true));
						}
						else
						{
							InventoryUpdate iu = new InventoryUpdate();
							if(destroyItem.getCount() == 0)
							{
								iu.addRemovedItem(destroyItem);
							}
							else
							{
								iu.addModifiedItem(destroyItem);
							}

							if(crystals != null)
							{
								iu.addItem(crystals);
							}

							if(scroll.getCount() == 0)
							{
								iu.addRemovedItem(scroll);
							}
							else
							{
								iu.addModifiedItem(scroll);
							}
							activeChar.sendPacket(iu);
						}

						WorldManager world = WorldManager.getInstance();
						world.removeObject(destroyItem);

						if(crystalId == 0)
						{
							activeChar.sendPacket(new EnchantResult(EnchantResult.NO_CRYSTAL, 0, 0));
						}
						else
						{
							activeChar.sendPacket(new EnchantResult(EnchantResult.FAIL, crystalId, count));
						}

						if(Config.LOG_ITEM_ENCHANTS)
						{
							_logEnchant.log(Level.INFO, EnchantLogFormatter.format("Success", new Object[]{
								activeChar, item, scroll, support, chance
							}));
						}
					}
				}
			}

			activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));

			InventoryUpdate iu = new InventoryUpdate();
			if(scroll.getCount() == 0)
			{
				iu.addRemovedItem(scroll);
			}
			else
			{
				iu.addModifiedItem(scroll);
			}

			if(item.getCount() == 0)
			{
				iu.addRemovedItem(item);
			}
			else
			{
				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);

            activeChar.broadcastUserInfo(UserInfoType.ENCHANTLEVEL);
            activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
            activeChar.sendPacket(new ExAdenaInvenCount(activeChar));

			HookManager.getInstance().notifyEvent(HookType.ON_ENCHANT_FINISH, activeChar.getHookContainer(), activeChar, success);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 58 RequestEnchantItem";
	}
}
