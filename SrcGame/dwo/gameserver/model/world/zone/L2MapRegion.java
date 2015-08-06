package dwo.gameserver.model.world.zone;

import dwo.config.Config;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class L2MapRegion
{
	private final String _name;
	private final String _town;
	private final int _locId;
	private final int _castle;
	private final int _bbs;
	private final Map<Race, String> _bannedRace = new HashMap<>();
	private List<int[]> _maps;
	private List<Location> _spawnLocs;
	private List<Location> _otherSpawnLocs;
	private List<Location> _chaoticSpawnLocs;
	private List<Location> _banishSpawnLocs;

	public L2MapRegion(String name, String town, int locId, int castle, int bbs)
	{
		_name = name;
		_town = town;
		_locId = locId;
		_castle = castle;
		_bbs = bbs;
	}

	public String getName()
	{
		return _name;
	}

	public String getTownName()
	{
		return _town;
	}

	public int getLocId()
	{
		return _locId;
	}

	public int getCastle()
	{
		return _castle;
	}

	public int getBbs()
	{
		return _bbs;
	}

	public void addMap(int x, int y)
	{
		if(_maps == null)
		{
			_maps = new ArrayList<>();
		}

		_maps.add(new int[]{x, y});
	}

	public List<int[]> getMaps()
	{
		return _maps;
	}

	public boolean isZoneInRegion(int x, int y)
	{
		if(_maps == null)
		{
			return false;
		}

		for(int[] map : _maps)
		{
			if(map[0] == x && map[1] == y)
			{
				return true;
			}
		}
		return false;
	}

	// Respawn
	public void addSpawn(int x, int y, int z)
	{
		if(_spawnLocs == null)
		{
			_spawnLocs = new ArrayList<>();
		}

		_spawnLocs.add(new Location(x, y, z));
	}

	public void addOtherSpawn(int x, int y, int z)
	{
		if(_otherSpawnLocs == null)
		{
			_otherSpawnLocs = new ArrayList<>();
		}

		_otherSpawnLocs.add(new Location(x, y, z));
	}

	public void addChaoticSpawn(int x, int y, int z)
	{
		if(_chaoticSpawnLocs == null)
		{
			_chaoticSpawnLocs = new ArrayList<>();
		}

		_chaoticSpawnLocs.add(new Location(x, y, z));
	}

	public void addBanishSpawn(int x, int y, int z)
	{
		if(_banishSpawnLocs == null)
		{
			_banishSpawnLocs = new ArrayList<>();
		}

		_banishSpawnLocs.add(new Location(x, y, z));
	}

	public List<Location> getSpawns()
	{
		return _spawnLocs;
	}

	public Location getSpawnLoc()
	{
		return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? _spawnLocs.get(Rnd.get(_spawnLocs.size())) : _spawnLocs.get(0);
	}

	public Location getOtherSpawnLoc()
	{
		if(_otherSpawnLocs != null)
		{
			return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? _otherSpawnLocs.get(Rnd.get(_otherSpawnLocs.size())) : _otherSpawnLocs.get(0);
		}
		else
		{
			return getSpawnLoc();
		}
	}

	public Location getChaoticSpawnLoc()
	{
		if(_chaoticSpawnLocs != null)
		{
			return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? _chaoticSpawnLocs.get(Rnd.get(_chaoticSpawnLocs.size())) : _chaoticSpawnLocs.get(0);
		}
		else
		{
			return getSpawnLoc();
		}
	}

	public Location getBanishSpawnLoc()
	{
		if(_banishSpawnLocs != null)
		{
			return Config.RANDOM_RESPAWN_IN_TOWN_ENABLED ? _banishSpawnLocs.get(Rnd.get(_banishSpawnLocs.size())) : _banishSpawnLocs.get(0);
		}
		else
		{
			return getSpawnLoc();
		}
	}

	public void addBannedRace(String race, String point)
	{
		_bannedRace.put(Race.valueOf(race), point);
	}

	public Map<Race, String> getBannedRace()
	{
		return _bannedRace;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
