package dwo.gameserver.model.world.zone.form;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.zone.L2ZoneForm;
import dwo.gameserver.util.Rnd;

import java.awt.*;

public class ZoneNPoly extends L2ZoneForm
{
	private Polygon _p;
	private int _z1;
	private int _z2;

	/**
	 * @param x
	 * @param y
	 * @param z1
	 * @param z2
	 */
	public ZoneNPoly(int[] x, int[] y, int z1, int z2)
	{
		_p = new Polygon(x, y, x.length);

		_z1 = Math.min(z1, z2);
		_z2 = Math.max(z1, z2);
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return _p.contains(x, y) && z >= _z1 && z <= _z2;
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		return _p.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.max(ax1, ax2) - Math.min(ax1, ax2), Math.max(ay1, ay2) - Math.min(ay1, ay2));
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		int[] _x = _p.xpoints;
		int[] _y = _p.ypoints;
		double test;
		double shortestDist = Math.pow(_x[0] - x, 2) + Math.pow(_y[0] - y, 2);

		for(int i = 1; i < _p.npoints; i++)
		{
			test = Math.pow(_x[i] - x, 2) + Math.pow(_y[i] - y, 2);
			if(test < shortestDist)
			{
				shortestDist = test;
			}
		}

		return Math.sqrt(shortestDist);
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
		int x;
		int y;

		int _minX = _p.getBounds().x;
		int _maxX = _p.getBounds().x + _p.getBounds().width;
		int _minY = _p.getBounds().y;
		int _maxY = _p.getBounds().y + _p.getBounds().height;

		x = Rnd.get(_minX, _maxX);
		y = Rnd.get(_minY, _maxY);

		int antiBlocker = 0;
		while(!_p.contains(x, y) && antiBlocker++ < 1000)
		{
			x = Rnd.get(_minX, _maxX);
			y = Rnd.get(_minY, _maxY);
		}

		return new int[]{x, y, GeoEngine.getInstance().getHeight(x, y, _z1)};
	}

	@Override
	public void visualizeZone(int z)
	{
		int[] _x = _p.xpoints;
		int[] _y = _p.ypoints;

		for(int i = 0; i < _p.npoints; i++)
		{
			int nextIndex = i + 1;
			// ending point to first one
			if(nextIndex == _x.length)
			{
				nextIndex = 0;
			}
			int vx = _x[nextIndex] - _x[i];
			int vy = _y[nextIndex] - _y[i];
			float lenght = (float) Math.sqrt(vx * vx + vy * vy);
			lenght /= STEP;
			for(int o = 1; o <= lenght; o++)
			{
				float k = o / lenght;
				dropDebugItem(PcInventory.ADENA_ID, 1, (int) (_x[i] + k * vx), (int) (_y[i] + k * vy), z);
			}
		}
	}
}