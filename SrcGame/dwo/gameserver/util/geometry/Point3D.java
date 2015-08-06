package dwo.gameserver.util.geometry;

import java.io.Serializable;

public class Point3D implements Serializable
{
	private static final long serialVersionUID = 4638345252031872576L;

	private volatile int _x;
	private volatile int _y;
	private volatile int _z;

	public Point3D(int pX, int pY, int pZ)
	{
		_x = pX;
		_y = pY;
		_z = pZ;
	}

	public Point3D(int pX, int pY)
	{
		_x = pX;
		_y = pY;
		_z = 0;
	}

	/**
	 * @param worldPosition
	 */
	public Point3D(Point3D worldPosition)
	{
		_x = worldPosition._x;
		_y = worldPosition._y;
		_z = worldPosition._z;
	}

	public static long distanceSquared(Point3D point1, Point3D point2)
	{
		long dx;
		long dy;
		dx = point1._x - point2._x;
		dy = point1._y - point2._y;
		return dx * dx + dy * dy;
	}

	public static boolean distanceLessThan(Point3D point1, Point3D point2, double distance)
	{
		return distanceSquared(point1, point2) < distance * distance;
	}

	public void setTo(Point3D point)
	{
		synchronized(this)
		{
			_x = point._x;
			_y = point._y;
			_z = point._z;
		}
	}

	@Override
	public int hashCode()
	{
		return _x ^ _y ^ _z;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o instanceof Point3D)
		{
			Point3D point3D = (Point3D) o;
			return point3D._x == _x && point3D._y == _y && point3D._z == _z;
		}
		return false;
	}

	@Override
	public Point3D clone()
	{
		return new Point3D(_x, _y, _z);
	}

	@Override
	public String toString()
	{
		return "(" + _x + ", " + _y + ", " + _z + ')';
	}

	public boolean equals(int pX, int pY, int pZ)
	{
		return _x == pX && _y == pY && _z == pZ;
	}

	public long distanceSquaredTo(Point3D point)
	{
		long dx;
		long dy;
		dx = _x - point._x;
		dy = _y - point._y;
		return dx * dx + dy * dy;
	}

	public int getX()
	{
		return _x;
	}

	public void setX(int pX)
	{
		synchronized(this)
		{
			_x = pX;
		}
	}

	public int getY()
	{
		return _y;
	}

	public void setY(int pY)
	{
		synchronized(this)
		{
			_y = pY;
		}
	}

	public int getZ()
	{
		return _z;
	}

	public void setZ(int pZ)
	{
		synchronized(this)
		{
			_z = pZ;
		}
	}

	public void setXYZ(int pX, int pY, int pZ)
	{
		synchronized(this)
		{
			_x = pX;
			_y = pY;
			_z = pZ;
		}
	}
}