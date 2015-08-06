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
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.stats.Env;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ConditionPlayerHasPet.
 */
public class ConditionPlayerHasPet extends Condition
{
	private final List<Integer> _controlItemIds;

	/**
	 * Instantiates a new condition player has pet.
	 *
	 * @param itemIds the item ids
	 */
	public ConditionPlayerHasPet(ArrayList<Integer> itemIds)
	{
		_controlItemIds = itemIds.size() == 1 && itemIds.get(0) == 0 ? null : itemIds;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		if(env.getCharacter().getPets().isEmpty())
		{
			return false;
		}

		if(!env.getCharacter().hasPet())
		{
			return false;
		}

		if(_controlItemIds == null)
		{
			return true;
		}

		L2ItemInstance controlItem = ((L2PetInstance) env.getCharacter().getItemPet()).getControlItem();
		if(controlItem == null)
		{
			return false;
		}

		return _controlItemIds.contains(controlItem.getItemId());
	}
}