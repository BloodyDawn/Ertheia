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
 * The Class ConditionPlayerPkCount.
 */
public class ConditionPlayerPkCount extends Condition
{
	public final int _pk;

	/**
	 * Instantiates a new condition player pk count.
	 *
	 * @param pk the pk
	 */
	public ConditionPlayerPkCount(int pk)
	{
		_pk = pk;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return ((L2PcInstance) env.getCharacter()).getPkKills() <= _pk;
	}
}