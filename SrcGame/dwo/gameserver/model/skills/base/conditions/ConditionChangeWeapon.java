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
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionChangeWeapon.
 *
 * @author nBd
 */
public class ConditionChangeWeapon extends Condition
{
	private final boolean _required;

	/**
	 * Instantiates a new condition change weapon.
	 *
	 * @param required the required
	 */
	public ConditionChangeWeapon(boolean required)
	{
		_required = required;
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 */
	@Override
	boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		if(_required)
		{
			L2Weapon weaponItem = env.getCharacter().getActiveWeaponItem();

			if(weaponItem == null)
			{
				return false;
			}

			if(weaponItem.getChangeWeaponId() == 0)
			{
				return false;
			}

			if(((L2PcInstance) env.getCharacter()).isEnchanting())
			{
				return false;
			}
		}
		return true;
	}

}