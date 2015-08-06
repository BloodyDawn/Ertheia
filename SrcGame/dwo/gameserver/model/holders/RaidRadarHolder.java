package dwo.gameserver.model.holders;

import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.10.12
 * Time: 22:36
 */

public class RaidRadarHolder
{
	private final long _fString;
	private final Location _loc;

	public RaidRadarHolder(Location loc, long fString)
	{
		_loc = loc;
		_fString = fString;
	}

	public long getFstring()
	{
		return _fString;
	}

	public Location getLoc()
	{
		return _loc;
	}
}