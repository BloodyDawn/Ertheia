package dwo.gameserver.model.holders;

import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: Bacek
 * Date: x.x.11
 * Time: xx:xx
 */

public class JumpHolder
{
	private final int _num;
	private final Location _loc;

	public JumpHolder(int num, Location loc)
	{
		_num = num;
		_loc = loc;
	}

	public int getNum()
	{
		return _num;
	}

	public Location getLoc()
	{
		return _loc;
	}
}