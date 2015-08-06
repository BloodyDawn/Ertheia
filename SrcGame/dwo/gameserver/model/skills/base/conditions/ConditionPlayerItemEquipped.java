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
package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.stats.Env;

import java.lang.reflect.Field;
import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class ConditionPlayerItemEquipped extends Condition
{
	private final List<Integer> _itemIds;

	public ConditionPlayerItemEquipped(List<Integer> itemIds)
	{
		_itemIds = itemIds;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2ItemInstance currentItem = env.getItem();
		Inventory inventory = env.getCharacter().getInventory();
		for(Field field : inventory.getClass().getFields())
		{
			if(field.isAnnotationPresent(Inventory.PaperdollSlot.class))
			{
				try
				{
					int slot = field.getInt(inventory);
					L2ItemInstance item = inventory.getPaperdollItem(slot);

					if(item != null && item.getObjectId() != currentItem.getObjectId() && _itemIds.contains(item.getItemId()))
					{
						return false;
					}
				}
				catch(IllegalAccessException e)
				{
				}
			}
		}

		return true;
	}
}