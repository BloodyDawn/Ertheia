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

import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author JIV
 */
public class ConditionPetType extends Condition
{
	private int petType;

	public ConditionPetType(int petType)
	{
		this.petType = petType;
	}

	@Override
	boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PetInstance))
		{
			return false;
		}

		int npcid = ((L2Summon) env.getCharacter()).getNpcId();

		if(PetDataTable.isHatchling(npcid) && (petType & L2Item.HATCHLING) == L2Item.HATCHLING)
		{
			return true;
		}
		if(PetDataTable.isWolf(npcid) && (petType & L2Item.WOLF) == L2Item.WOLF)
		{
			return true;
		}
		if(PetDataTable.isEvolvedWolf(npcid) && (petType & L2Item.GROWN_WOLF) == L2Item.GROWN_WOLF)
		{
			return true;
		}
		if(PetDataTable.isStrider(npcid) && (petType & L2Item.STRIDER) == L2Item.STRIDER)
		{
			return true;
		}
		if(PetDataTable.isBaby(npcid) && (petType & L2Item.BABY) == L2Item.BABY)
		{
			return true;
		}
		return PetDataTable.isImprovedBaby(npcid) && (petType & L2Item.IMPROVED_BABY) == L2Item.IMPROVED_BABY;

	}

}
