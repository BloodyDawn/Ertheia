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
package dwo.gameserver.model.items.multisell;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;

/**
 * @author DS
 */
public class PreparedEntry extends Entry
{
	public static Logger _log = LogManager.getLogger(PreparedEntry.class);

	private long _taxAmount;

	public PreparedEntry(Entry template, L2ItemInstance item, boolean applyTaxes, boolean keepEnchant, boolean isAllowAugmentedItems, boolean isAllowElementalItems, double taxRate)
	{
		try
		{
			_entryId = template.getEntryId() * 100000;
			if(keepEnchant && item != null)
			{
				_entryId += item.getEnchantLevel();
			}

			ItemInfo info = null;
			long adenaAmount = 0;

			_ingredients = new ArrayList<>(template.getIngredients().size());
			for(Ingredient ing : template.getIngredients())
			{
				if(ing.getItemId() == ADENA_ID)
				{
					// Tax ingredients added only if taxes enabled
					// count based on the template adena/ancient adena count
					if(applyTaxes)
					{
						_taxAmount += Math.round(ing.getItemCount() * taxRate);
					}

					adenaAmount += ing.getItemCount();
				}
				else if((keepEnchant || isAllowAugmentedItems || isAllowElementalItems) && item != null && ing.isArmorOrWeapon())
				{
					info = new ItemInfo(item, keepEnchant, isAllowAugmentedItems, isAllowElementalItems);
					Ingredient newIngredient = ing.clone();
					newIngredient.setItemInfo(info);
					_ingredients.add(newIngredient);
				}
				else if(!keepEnchant && ing.isArmorOrWeapon() && ing.getEnchantLvl() > 0)
				{
					info = new ItemInfo(ing.getEnchantLvl());
					Ingredient newIngredient = ing.clone();
					newIngredient.setItemInfo(info);
					_ingredients.add(newIngredient);
				}
				else
				{
					_ingredients.add(ing);
				}

			}

			adenaAmount += _taxAmount;

			if(adenaAmount > 0)
			{
				_ingredients.add(new Ingredient(ADENA_ID, adenaAmount, 100, 0));
			}

			// now copy products
			_products = new ArrayList<>(template.getProducts().size());
			for(Ingredient ing : template.getProducts())
			{
				if(!ing.isStackable())
				{
					_stackable = false;
				}

				if((keepEnchant || isAllowAugmentedItems || isAllowElementalItems) && ing.isArmorOrWeapon())
				{
					Ingredient newProduct = ing.clone();
					newProduct.setItemInfo(info);
					_products.add(newProduct);
				}
				else if(!keepEnchant && ing.isArmorOrWeapon() && ing.getEnchantLvl() > 0)
				{
					Ingredient newProduct = ing.clone();
					info = new ItemInfo(ing.getEnchantLvl());
					newProduct.setItemInfo(info);
					_products.add(newProduct);
				}
				else
				{
					_products.add(ing);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, PreparedEntry.class.getName(), e);
		}
	}

	@Override
	public long getTaxAmount()
	{
		return _taxAmount;
	}
}