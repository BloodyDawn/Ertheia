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
package dwo.gameserver.model.actor.status;

import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.worldstat.CategoryType;

public class AttackableStatus extends NpcStatus
{
	public AttackableStatus(L2Attackable activeChar)
	{
		super(activeChar);
	}

	@Override
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}

	@Override
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if(getActiveChar().isDead())
		{
			return;
		}

		if(value > 0)
		{
			if(getActiveChar().isOverhit())
			{
				getActiveChar().setOverhitValues(attacker, value);
			}
			else
			{
				getActiveChar().overhitEnabled(false);
			}
		}
		else
		{
			getActiveChar().overhitEnabled(false);
		}

		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);

		// Обновляем значения статистики, если монстра ударил игрок
		if(attacker != null && attacker instanceof L2PcInstance && attacker.isAwakened())
		{
			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				attacker.getActingPlayer().updateWorldStatistic(CategoryType.DAMAGE_TO_MONSTERS, null, (long) value);
			}
		}

		if(!getActiveChar().isDead())// And the attacker's hit didn't kill the mob, clear the over-hit flag
		{
			getActiveChar().overhitEnabled(false);
		}
	}

	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) super.getActiveChar();
	}
}