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
package dwo.gameserver.model.actor.stat;

import dwo.gameserver.model.actor.L2Vehicle;

public class VehicleStat extends CharStat
{
	private float _moveSpeed;
	private int _rotationSpeed;

	public VehicleStat(L2Vehicle activeChar)
	{
		super(activeChar);
	}

	@Override
	public float getMoveSpeed()
	{
		return _moveSpeed;
	}

	public void setMoveSpeed(float speed)
	{
		_moveSpeed = speed;
	}

	public int getRotationSpeed()
	{
		return _rotationSpeed;
	}

	public void setRotationSpeed(int speed)
	{
		_rotationSpeed = speed;
	}
}