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

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.skills.stats.Env;

import java.util.List;

/**
 * The Class ConditionTargetRaceId.
 *
 * @author nBd
 */

public class ConditionTargetRaceId extends Condition
{
	private final List<Integer> _raceIds;

	/**
	 * Instantiates a new condition target race id.
	 *
	 * @param raceId the race id
	 */
	public ConditionTargetRaceId(List<Integer> raceId)
	{
		_raceIds = raceId;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getTarget() instanceof L2Npc))
		{
			return false;
		}
		return _raceIds.contains(((L2Npc) env.getTarget()).getTemplate().getRace().ordinal());
	}
}
