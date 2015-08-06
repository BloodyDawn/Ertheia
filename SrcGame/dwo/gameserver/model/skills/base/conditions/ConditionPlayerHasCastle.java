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
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionPlayerHasCastle.
 *
 * @author MrPoke
 */
public class ConditionPlayerHasCastle extends Condition
{

	private final int _castle;

	/**
	 * Instantiates a new condition player has castle.
	 *
	 * @param castle the castle
	 */
	public ConditionPlayerHasCastle(int castle)
	{
		_castle = castle;
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
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		L2Clan clan = ((L2PcInstance) env.getCharacter()).getClan();
		if(clan == null)
		{
			return _castle == 0;
		}

		// Any castle
		if(_castle == -1)
		{
			return clan.getCastleId() > 0;
		}

		return clan.getCastleId() == _castle;
	}
}