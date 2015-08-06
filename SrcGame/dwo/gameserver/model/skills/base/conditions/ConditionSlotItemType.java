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

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionSlotItemType.
 *
 * @author mkizub
 */
public class ConditionSlotItemType extends ConditionInventory
{
	private final int _mask;

	/**
	 * Instantiates a new condition slot item type.
	 *
	 * @param slot the slot
	 * @param mask the mask
	 */
	public ConditionSlotItemType(int slot, int mask)
	{
		super(slot);
		_mask = mask;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}
		Inventory inv = ((L2PcInstance) env.getCharacter()).getInventory();
		L2ItemInstance item = inv.getPaperdollItem(_slot);
		if(item == null)
		{
			return false;
		}
		return (item.getItem().getItemMask() & _mask) != 0;
	}
}
