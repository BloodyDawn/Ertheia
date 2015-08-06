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

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ConditionPlayerServitorNpcId.
 */
public class ConditionPlayerServitorNpcId extends Condition
{
	private final List<Integer> _npcIds;

	/**
	 * Instantiates a new condition player servitor npc id.
	 *
	 * @param npcIds the npc ids
	 */
	public ConditionPlayerServitorNpcId(ArrayList<Integer> npcIds)
	{
		_npcIds = npcIds.size() == 1 && npcIds.get(0) == 0 ? null : npcIds;
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
		boolean isHaveRightServitor = false;
		for(L2Summon pet : env.getCharacter().getPets())
		{
			if(_npcIds.contains(pet.getNpcId()))
			{
				isHaveRightServitor = true;
				break;
			}
		}
		return _npcIds == null || isHaveRightServitor;
	}
}