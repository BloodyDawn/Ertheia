package dwo.gameserver.model.player.teleport;

import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.01.12
 * Time: 23:12
 */

public class TeleportLocation
{
	private int _price;
	private int _item;
	private String _fstring;
	private Location _loc;
	private boolean _isNoble;

	public TeleportLocation(int item, int price, String fstring, Location loc, boolean isNoble)
	{
		_item = item;
		_price = price;
		_fstring = fstring;
		_loc = loc;
		_isNoble = isNoble;
	}

	public int getItemId()
	{
		return _item;
	}

	public int getPrice()
	{
		return _price;
	}

	public String getFstring()
	{
		return _fstring;
	}

	public Location getLocation()
	{
		return _loc;
	}

	public boolean isNoble()
	{
		return _isNoble;
	}
}
