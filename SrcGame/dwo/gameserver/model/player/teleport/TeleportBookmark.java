package dwo.gameserver.model.player.teleport;

import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.01.12
 * Time: 9:15
 */

public class TeleportBookmark
{
	private int _id;
	private int _icon;
	private String _name;
	private String _tag;

	private Location _loc;

	public TeleportBookmark(int id, Location loc, int icon, String tag, String name)
	{
		_id = id;
		_loc = loc;
		_icon = icon;
		_name = name;
		_tag = tag;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public Location getLoc()
	{
		return _loc;
	}

	public void setLoc(Location loc)
	{
		_loc = loc;
	}

	public int getIcon()
	{
		return _icon;
	}

	public void setIcon(int icon)
	{
		_icon = icon;
	}

	public String getTag()
	{
		return _tag;
	}

	public void setTag(String tag)
	{
		_tag = tag;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}
}
