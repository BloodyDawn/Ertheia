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
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionPlayerInvSize.
 *
 * @author Kerberos
 */
public class ConditionPlayerInvSize extends Condition
{

	private final int _size;

	/**
	 * Instantiates a new condition player inv size.
	 *
	 * @param size the size
	 */
	public ConditionPlayerInvSize(int size)
	{
		_size = size;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getCharacter() instanceof L2PcInstance)
		{
			return ((L2PcInstance) env.getCharacter()).getInventory().getSize(false) <= ((L2PcInstance) env.getCharacter()).getInventoryLimit() - _size;
		}
		return true;
	}
}
