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

import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.Rnd;

/**
 * The Class ConditionGameChance.
 *
 * @author Advi
 */
public class ConditionGameChance extends Condition
{
	private final int _chance;

	/**
	 * Instantiates a new condition game chance.
	 *
	 * @param chance the chance
	 */
	public ConditionGameChance(int chance)
	{
		_chance = chance;
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(Env env)
	{
		return Rnd.getChance(_chance);
	}
}