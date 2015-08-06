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

import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.zone.L2ZoneType;

import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class ConditionPlayerInsideZoneId extends Condition
{
	private final List<Integer> _zones;

	public ConditionPlayerInsideZoneId(List<Integer> zones)
	{
		_zones = zones;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		for(L2ZoneType zone : ZoneManager.getInstance().getZones(env.getCharacter()))
		{
			if(_zones.contains(zone.getId()))
			{
				return true;
			}
		}
		return false;
	}
}