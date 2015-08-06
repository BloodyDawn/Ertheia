package dwo.gameserver.model.world.npc.spawn;

import dwo.gameserver.model.world.zone.Location;

import java.util.List;

public class CastleTowerSpawn
{
	private final int _npcId;
	private final Location _location;
	private List<Integer> _zoneList;
	private int _upgradeLevel;

	public CastleTowerSpawn(int npcId, Location location)
	{
		_location = location;
		_npcId = npcId;
	}

	public CastleTowerSpawn(int npcId, Location location, List<Integer> zoneList)
	{
		_location = location;
		_npcId = npcId;
		_zoneList = zoneList;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public Location getLocation()
	{
		return _location;
	}

	public List<Integer> getZoneList()
	{
		return _zoneList;
	}

	public int getUpgradeLevel()
	{
		return _upgradeLevel;
	}

	public void setUpgradeLevel(int level)
	{
		_upgradeLevel = level;
	}
}