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

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

import java.util.List;

/**
 * The Class ConditionPlayerInstanceId.
 */
public class ConditionPlayerInstanceId extends Condition
{
	private final List<Integer> _instanceIds;

	/**
	 * Instantiates a new condition player instance id.
	 *
	 * @param instanceIds the instance ids
	 */
	public ConditionPlayerInstanceId(List<Integer> instanceIds)
	{
		_instanceIds = instanceIds;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		int instanceId = env.getCharacter().getInstanceId();
		if(instanceId <= 0)
		{
			return false; // player not in instance
		}

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld((L2PcInstance) env.getCharacter());
		if(world == null || world.instanceId != instanceId)
		{
			return false; // player in the different instance
		}

		return _instanceIds.contains(world.templateId);
	}
}