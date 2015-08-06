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
package dwo.gameserver.model.world.zone.form;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.zone.L2ZoneForm;
import dwo.gameserver.util.Rnd;

/**
 * A primitive circular zone
 *
 * @author durgus
 */
public class ZoneCylinder extends L2ZoneForm
{
	private int _x;
	private int _y;
	private int _z1;
	private int _z2;
	private int _rad;
	private int _radS;

	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
		_rad = rad;
		_radS = rad * rad;
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return !(Math.pow(_x - x, 2) + Math.pow(_y - y, 2) > _radS || z < _z1 || z > _z2);
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		// Circles point inside the rectangle?
		if(_x > ax1 && _x < ax2 && _y > ay1 && _y < ay2)
		{
			return true;
		}

		double powX1 = Math.pow(ax1 - _x, 2);
		double powX2 = Math.pow(ax2 - _x, 2);
		double powY1 = Math.pow(ay1 - _y, 2);
		double powY2 = Math.pow(ay2 - _y, 2);

		// Any point of the rectangle intersecting the Circle?
		if(powX1 + powY1 < _radS)
		{
			return true;
		}
		if(powX1 + powY2 < _radS)
		{
			return true;
		}
		if(powX2 + powY1 < _radS)
		{
			return true;
		}
		if(powX2 + powY2 < _radS)
		{
			return true;
		}

		// Collision on any side of the rectangle?
		if(_x > ax1 && _x < ax2)
		{
			if(Math.abs(_y - ay2) < _rad)
			{
				return true;
			}
			if(Math.abs(_y - ay1) < _rad)
			{
				return true;
			}
		}
		if(_y > ay1 && _y < ay2)
		{
			if(Math.abs(_x - ax2) < _rad)
			{
				return true;
			}
			if(Math.abs(_x - ax1) < _rad)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		return Math.sqrt(Math.pow(_x - x, 2) + Math.pow(_y - y, 2)) - _rad;
	}

    /* getLowZ() / getHighZ() - These two functions were added to cope with the demand of the new
      * fishing algorithms, wich are now able to correctly place the hook in the water, thanks to getHighZ().
      * getLowZ() was added, considering potential future modifications.
      */

	@Override
	public int getLowZ()
	{
		return _z1;
	}

	@Override
	public int getHighZ()
	{
		return _z2;
	}

	@Override
	public int[] getRandomPosition()
	{
		// TODO We need a better calculation!
		// For now the easier one is faster and have better results.

		int x = Integer.MAX_VALUE;
		int y = Integer.MAX_VALUE;

		while(x == Integer.MAX_VALUE || !isInsideZone(x, y, _z1))
		{
			x = _x + Rnd.get(-_rad, _rad);
			y = _y + Rnd.get(-_rad, _rad);
		}

		return new int[]{
			x, y, GeoEngine.getInstance().getHeight(x, y, _z1)
		};
	}

	@Override
	public void visualizeZone(int z)
	{
		int count = (int) (2 * Math.PI * _rad / STEP);
		double angle = 2 * Math.PI / count;
		for(int i = 0; i < count; i++)
		{
			int x = (int) (Math.cos(angle * i) * _rad);
			int y = (int) (Math.sin(angle * i) * _rad);
			dropDebugItem(PcInventory.ADENA_ID, 1, _x + x, _y + y, z);
		}
	}
}
