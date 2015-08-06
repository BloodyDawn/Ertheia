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
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.L2Augmentation;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.items.multisell.Entry;
import dwo.gameserver.model.items.multisell.Ingredient;
import dwo.gameserver.model.items.multisell.PreparedListContainer;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class MultiSellChoose.
 */
public class MultiSellChoose extends L2GameClientPacket
{
	private int _listId;
	private int _entryId;
	private long _amount;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readQ();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getMultiSell().tryPerformAction(FloodAction.MULTISELL_CHOOSE))
		{
			player.setMultiSell(null);
			return;
		}

		if(_amount < 1 || _amount > 5000)
		{
			player.setMultiSell(null);
			return;
		}

		PreparedListContainer list = player.getMultiSell();
		if(list == null || list.getListId() != _listId)
		{
			player.setMultiSell(null);
			return;
		}

		L2Npc target = player.getLastFolkNPC();
		if(!player.isGM() && list.isNpcRequied() && (target == null || !list.checkNpcObjectId(target.getObjectId()) || !target.canInteract(player)))
		{
			player.setMultiSell(null);
			return;
		}

		for(Entry entry : list.getEntries())
		{
			if(entry.getEntryId() == _entryId)
			{
				if(!entry.isStackable() && _amount > 1)
				{
					_log.log(Level.ERROR, "Character: " + player.getName() + " is trying to set amount > 1 on non-stackable multisell, id:" + _listId + ':' + _entryId);
					player.setMultiSell(null);
					return;
				}

				PcInventory inv = player.getInventory();

				int slots = 0;
				int weight = 0;
				for(Ingredient e : entry.getProducts())
				{
					if(e.getItemId() < 0) // special
					{
						continue;
					}

					if(!e.isStackable())
					{
						slots += e.getItemCount() * _amount;
					}
					else if(player.getInventory().getItemByItemId(e.getItemId()) == null)
					{
						slots++;
					}
					weight += e.getItemCount() * _amount * e.getWeight();
				}

				if(!inv.validateWeight(weight))
				{
					player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
					return;
				}

				if(!inv.validateCapacity(slots))
				{
					player.sendPacket(SystemMessageId.SLOTS_FULL);
					return;
				}

				List<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
				// Generate a list of distinct ingredients and counts in order to check if the correct item-counts
				// are possessed by the player
				boolean newIng;
				for(Ingredient e : entry.getIngredients())
				{
					newIng = true;
					// at this point, the template has already been modified so that enchantments are properly included
					// whenever they need to be applied.  Uniqueness of items is thus judged by item id AND enchantment level
					for(int i = ingredientsList.size(); --i >= 0; )
					{
						Ingredient ex = ingredientsList.get(i);
						// if the item was already added in the list, merely increment the count
						// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
						if(ex.getItemId() == e.getItemId() && ex.getEnchantLevel() == e.getEnchantLevel())
						{
							if(ex.getItemCount() + e.getItemCount() > Integer.MAX_VALUE)
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
								return;
							}
							// two same ingredients, merge into one and replace old
							Ingredient ing = ex.clone();
							ing.setItemCount(ex.getItemCount() + e.getItemCount());
							ingredientsList.set(i, ing);
							newIng = false;
							break;
						}
					}
					if(newIng)
					{
						// if it's a new ingredient, just store its info directly (item id, count, enchantment)
						ingredientsList.add(e);
					}
				}

				// now check if the player has sufficient items in the inventory to cover the ingredients' expences
				for(Ingredient e : ingredientsList)
				{
					if(e.getItemCount() * _amount > Integer.MAX_VALUE)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return;
					}
					if(e.getItemId() < 0)
					{
						if(!MultiSellData.checkSpecialIngredient(e.getItemId(), e.getItemCount() * _amount, player))
						{
							return;
						}
					}
					else
					{
						long required = Config.ALT_BLACKSMITH_USE_RECIPES || !list.isKeepEnchant() ? e.getItemCount() * _amount : e.getItemCount();
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
						if(list.isKeepEnchant())
						{
							if(inv.getInventoryItemCount(e.getItemId(), e.getEnchantLevel(), false) < required)
							{
								sm.addItemName(e.getTemplate());
								sm.addNumber((int) required);
								player.sendPacket(sm);
								return;
							}
						}
						else
						{
							if(inv.getInventoryItemCount(e.getItemId(), e.getEnchantLevel(), false) < required)
							{
								if(e.getEnchantLevel() > 0)
								{
									sm.addString("+" + e.getEnchantLevel() + ' ' + e.getTemplate().getName());
								}
								else
								{
									if(e.getTemplate() != null)
									{
										sm.addItemName(e.getTemplate());
									}
									else
									{
										return;
									}
								}
								sm.addNumber((int) required);
								player.sendPacket(sm);
								return;
							}
						}
					}
				}

				FastList<L2Augmentation> augmentation = FastList.newInstance();
				Elementals[] elemental = null;
				/** All ok, remove items and add final product */

				try
				{
					for(Ingredient e : entry.getIngredients())
					{
						if(e.getItemId() < 0)
						{
							if(!MultiSellData.getSpecialIngredient(e.getItemId(), e.getItemCount() * _amount, player))
							{
								return;
							}
						}
						else
						{
							L2ItemInstance itemToTake = null;
							if(e.getEnchantLevel() > 0)
							{
								List<L2ItemInstance> itemsToTake = inv.getItemsByItemId(e.getItemId());
								if(itemsToTake == null || itemsToTake.isEmpty())
								{
									_log.log(Level.ERROR, "Character: " + player.getName() + " is trying to cheat in multisell, id:" + _listId + ':' + _entryId);
									player.setMultiSell(null);
									return;
								}

								for(L2ItemInstance i : itemsToTake)
								{
									if(i.getEnchantLevel() == e.getEnchantLevel())
									{
										itemToTake = i;
										break;
									}
								}
							}
							else
							{
								itemToTake = inv.getItemByItemId(e.getItemId()); // initialize and initial guess for the item to take.
							}

							if(itemToTake == null)
							{ //this is a cheat, transaction will be aborted and if any items already taken will not be returned back to inventory!
								_log.log(Level.ERROR, "Character: " + player.getName() + " is trying to cheat in multisell, id:" + _listId + ':' + _entryId);
								player.setMultiSell(null);
								return;
							}

							// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
							if(itemToTake.isStackable())
							{
								if(!player.destroyItem(ProcessType.MULTISELL, itemToTake.getObjectId(), e.getItemCount() * _amount, player.getTarget(), true))
								{
									player.setMultiSell(null);
									return;
								}
							}
							else
							{
								// for non-stackable items, one of two scenaria are possible:
								// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
								// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level

								// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
								if(list.isKeepEnchant() || list.isAllowAugmentedItems() || list.isAllowElementalItems())
								{
									// loop through this list and remove (one by one) each item until the required amount is taken.
									L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantLevel(), false);
									for(int i = 0; i < e.getItemCount() * _amount; i++)
									{
										if(inventoryContents[i].isAugmented() && list.isAllowAugmentedItems())
										{
											augmentation.add(inventoryContents[i].getAugmentation());
										}

										if(inventoryContents[i].getElementals() != null && list.isAllowElementalItems())
										{
											elemental = inventoryContents[i].getElementals();
										}

										if(!player.destroyItem(ProcessType.MULTISELL, inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
										{
											player.setMultiSell(null);
											return;
										}
									}
								}
								else
								// b) enchantment is not maintained.  Get the instances with the LOWEST enchantment level
								{
									// choice 1.  Small number of items exchanged.  No sorting.
									for(int i = 1; i <= e.getItemCount() * _amount; i++)
									{
										L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), false);

										itemToTake = inventoryContents[0];
										// get item with the LOWEST enchantment level  from the inventory...
										// +0 is lowest by default...
										if(itemToTake.getEnchantLevel() > 0)
										{
											for(L2ItemInstance item : inventoryContents)
											{
												if(item.getEnchantLevel() < itemToTake.getEnchantLevel())
												{
													itemToTake = item;
													// nothing will have enchantment less than 0. If a zero-enchanted
													// item is found, just take it
													if(itemToTake.getEnchantLevel() == 0)
													{
														break;
													}
												}
											}
										}
										if(!player.destroyItem(ProcessType.MULTISELL, itemToTake.getObjectId(), 1, player.getTarget(), true))
										{
											player.setMultiSell(null);
											return;
										}
									}
								}
							}
						}
					}

					/**
					 * Служит для генерации общего интервала,
					 * суммы шансов для более точного определения
					 * получаемого предмета (метод Рулетки)
					 */
					int rndNum = 0;
					int chanceInterval = 0;
					int chance = 0;
					if(list.isChanceBuy())
					{
						for(Ingredient e : entry.getProducts())
						{
							if(e.getChance() != 0)
							{
								rndNum += e.getChance();
							}
						}
						chance = Rnd.get(rndNum);
					}

					// Выдаем предметы из продуктов
					for(Ingredient e : entry.getProducts())
					{
						// Обработчик для специальных продуктов (типа Слава/ПкПоинты)
						if(e.getItemId() < 0)
						{
							MultiSellData.addSpecialProduct(e.getItemId(), e.getItemCount() * _amount, player);
						}
						// Тип мультиселла: 1 (новый)
						else if(list.isChanceBuy())
						{
							/**
							 * Новые мультиселы типа 1 первым продуктом содержат декоративную иконку
							 * которая отображается в левой панельке мультиселла
							 * Не добавляем её в продукты (обозначается в мультислле chance'ом равным 0)
							 * TODO: Возможно придется сделать для !isStackable(), но так как нету
							 * TODO: пока мультиселлов с примером работы, пропускаем это
							 */
							if(e.getChance() == 0)
							{
								continue;
							}
							if(chanceInterval <= chance && chance <= chanceInterval + e.getChance())
							{
								if(e.isStackable())
								{
									inv.addItem(ProcessType.MULTISELL, e.getItemId(), e.getItemCount(), player, player.getTarget());
								}
								else
								{
									L2ItemInstance product;
									for(int i = 0; i < e.getItemCount(); i++)
									{
										product = inv.addItem(ProcessType.MULTISELL, e.getItemId(), 1, player, player.getTarget());
										if(list.isKeepEnchant() || list.isAllowAugmentedItems() || list.isAllowElementalItems())
										{
											if(list.isKeepEnchant())
											{
												product.setEnchantLevel(e.getEnchantLevel());
											}

											if(i < augmentation.size())
											{
												product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill()));
											}

											if(elemental != null)
											{
												for(Elementals elm : elemental)
												{
													product.setElementAttr(elm.getElement(), elm.getValue());
												}
											}
										}
										product.updateDatabase();
									}
								}

								if(e.getItemCount() > 1)
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(e.getItemId()).addItemNumber(e.getItemCount() * _amount));
								}
								else
								{
									if(list.isKeepEnchant() && e.getEnchantLevel() > 0 || !list.isKeepEnchant() && e.getEnchantLevel() > 0)
									{
										player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addItemNumber(e.getEnchantLevel()).addItemName(e.getItemId()));
									}
									else
									{
										player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(e.getItemId()));
									}
								}
								break;
							}
							else
							{
								chanceInterval += e.getChance();
							}
						}
						// Тип мультиселла: 0 (старый)
						else
						{
							if(e.isStackable())
							{
								inv.addItem(ProcessType.MULTISELL, e.getItemId(), e.getItemCount() * _amount, player, player.getTarget());
							}
							else
							{
								L2ItemInstance product;
								for(int i = 0; i < e.getItemCount() * _amount; i++)
								{
									product = inv.addItem(ProcessType.MULTISELL, e.getItemId(), 1, player, player.getTarget());

									if(list.isKeepEnchant() || list.isAllowAugmentedItems() || list.isAllowElementalItems())
									{
										if(list.isKeepEnchant())
										{
											product.setEnchantLevel(e.getEnchantLevel());
										}

										if(i < augmentation.size())
										{
											product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill()));
										}

										if(elemental != null)
										{
											for(Elementals elm : elemental)
											{
												product.setElementAttr(elm.getElement(), elm.getValue());
											}
										}
									}
									product.updateDatabase();
								}
							}

							if(e.getItemCount() * _amount > 1)
							{
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(e.getItemId()).addItemNumber(e.getItemCount() * _amount));
							}
							else
							{
								if(list.isKeepEnchant() && e.getEnchantLevel() > 0 || !list.isKeepEnchant() && e.getEnchantLevel() > 0)
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addItemNumber(e.getEnchantLevel()).addItemName(e.getItemId()));
								}
								else
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(e.getItemId()));
								}
							}
						}
					}
					player.sendPacket(new ItemList(player, false));
					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
				}
				finally
				{
					FastList.recycle(augmentation);
				}

				// finally, give the tax to the castle...
				if(entry.getTaxAmount() > 0)
				{
					target.getCastle().addToTreasury(entry.getTaxAmount() * _amount);
				}

				break;
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] A7 MultiSellChoose";
	}
}
