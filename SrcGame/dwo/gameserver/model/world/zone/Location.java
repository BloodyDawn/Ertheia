package dwo.gameserver.model.world.zone;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.util.Rnd;

public class Location
{
	public int _id;
	public int _x;
	public int _y;
	public int _z;
	public int _heading;

    public static final Location[] EMPTY_LOCATION = new Location[0];

	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	public Location(int[] loc)
	{
		_x = loc[0];
		_y = loc[1];
		_z = loc[2];
	}

	public Location(L2Object obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
	}

	public Location(L2Character obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_heading = obj.getHeading();
	}

	public Location(StatsSet set)
	{
		_x = set.getInteger("x");
		_y = set.getInteger("y");
		_z = set.getInteger("z");
		_heading = set.getInteger("heading", 0);
		_id = set.getInteger("id", 0);
	}

	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}

	public Location(String loc)
	{
		String[] temp = loc.split(",");
		_x = Integer.valueOf(temp[0]);
		_y = Integer.valueOf(temp[1]);
		_z = Integer.valueOf(temp[2]);
	}

    public void set(int _x, int _y, int _z)
    {
        this._x = _x;
        this._y = _y;
        this._z = _z;
    }

    public void set(int _x, int _y, int _z, int _heading)
    {
        this._x = _x;
        this._y = _y;
        this._z = _z;
        this._heading = _heading;
    }

    public void set(Location l)
    {
        this._x = l.getX();
        this._y = l.getY();
        this._z = l.getZ();
    }

    public Location setH(int _heading)
    {
        this._heading = _heading;
        return this;
    }

    public boolean equals(int _x, int _y, int _z)
    {
        return _x == this._x && _y == this._y && _z == this._z;
    }

    public boolean equals(Location loc)
    {
        return loc.getX() == this._x && loc.getY() == this._y && loc.getZ() == this._z;
    }
    
    public Location world2geo()
    {
        _x = (_x - WorldManager.MAP_MIN_X) >> 4;
        _y = (_y - WorldManager.MAP_MIN_Y) >> 4;
        return this;
    }

    public Location geo2world()
    {
        _x = (_x << 4) + WorldManager.MAP_MIN_X + 8;
        _y = (_y << 4) + WorldManager.MAP_MIN_Y + 8;
        return this;
    }

    public static Location coordsRandomize(int x, int y, int z, int heading, int radiusmin, int radiusmax)
    {
        if (radiusmax == 0 || radiusmax < radiusmin)
        {
            return new Location(x, y, z, heading);
        }
        int radius = Rnd.get(radiusmin, radiusmax);
        double angle = Rnd.nextDouble() * 2 * Math.PI;
        return new Location((int) (x + radius * Math.cos(angle)), (int) (y + radius * Math.sin(angle)), z, heading);
    }
    
	public int getX()
	{
		return _x;
	}

	/**
	 * @param x The x to set.
	 */
	public void setX(int x)
	{
		_x = x;
	}

	public int getY()
	{
		return _y;
	}

	/**
	 * @param y The y to set.
	 */
	public void setY(int y)
	{
		_y = y;
	}

	public int getZ()
	{
		return _z;
	}

	/**
	 * @param z The z to set.
	 */
	public void setZ(int z)
	{
		_z = z;
	}

	public int getHeading()
	{
		return _heading;
	}

	/**
	 * @param heading The heading to set.
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int val)
	{
		_id = val;
	}

	@Override
	public String toString()
	{
		return "X: " + _x + " Y: " + _y + " Z: " + _z + " H: " + _heading;
	}

    public String toXYZString()
{
    return _x + "," + _y + "," + _z;
}
}