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

import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import org.jetbrains.annotations.Nullable;

/**
 * @author DS
 */
public class Ingredient implements Cloneable
{
	private int _itemId;
	private long _itemCount;
	private int _enchantLvl;
	private int _chance;

	private L2Item _template;
	private ItemInfo _itemInfo;

	public Ingredient(int itemId, long itemCount, int enchantLvl, int chance)
	{
		_itemId = itemId;
		_itemCount = itemCount;
		_enchantLvl = enchantLvl;
		_chance = chance;
		if(_itemId > 0)
		{
			_template = ItemTable.getInstance().getTemplate(_itemId);
		}
	}

	@Override
	public Ingredient clone()
	{
		try
		{
			return (Ingredient) super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return null; // should not happens
		}
	}

	@Nullable
	public L2Item getTemplate()
	{
		return _template;
	}

	public ItemInfo getItemInfo()
	{
		if(_itemInfo == null)
		{
			_itemInfo = new ItemInfo(0);
		}

		return _itemInfo;
	}

	public void setItemInfo(ItemInfo info)
	{
		_itemInfo = info;
	}

	public int getEnchantLevel()
	{
		return _itemInfo != null ? _itemInfo.getEnchantLevel() : 0;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public void setItemCount(long itemCount)
	{
		_itemCount = itemCount;
	}

	public boolean isStackable()
	{
		return _template == null || _template.isStackable();
	}

	public boolean isArmorOrWeapon()
	{
		return _template instanceof L2Armor || _template instanceof L2Weapon;
	}

	public int getWeight()
	{
		return _template == null ? 0 : _template.getWeight();
	}

	public int getEnchantLvl()
	{
		return _enchantLvl;
	}

	public void setEnchantLvl(int enchantLvl)
	{
		_enchantLvl = enchantLvl;
	}

	public int getChance()
	{
		return _chance;
	}
}