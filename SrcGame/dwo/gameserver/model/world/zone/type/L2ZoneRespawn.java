package dwo.gameserver.model.world.zone.type;

import dwo.config.Config;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public abstract class L2ZoneRespawn extends L2ZoneType
{
	private List<Location> _spawnLocs;
	private List<Location> _otherSpawnLocs;
	private List<Location> _chaoticSpawnLocs;
	private List<Location> _banishSpawnLocs;
	private List<Location> _observerSpawnLocs;

	protected L2ZoneRespawn(int id)
	{
		super(id);
	}

	public void parseLoc(int x, int y, int z, String type)
	{
		if(type == null || type.isEmpty())
		{
			addSpawn(x, y, z);
		}
		else
		{
			switch(type)
			{
				case "other":
					addOtherSpawn(x, y, z);
					break;
				case "chaotic":
					addChaoticSpawn(x, y, z);
					break;
				case "banish":
					addBanishSpawn(x, y, z);
					break;
				case "observer":
					addObserverSpawn(x, y, z);
					break;
				default:
					_log.log(Level.WARN, "L2ZoneRespawn: Unknown location type: " + type);
			}
		}
	}

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

	public void addObserverSpawn(int x, int y, int z)
	{
		if(_observerSpawnLocs == null)
		{
			_observerSpawnLocs = new ArrayList<>();
		}

		_observerSpawnLocs.add(new Location(x, y, z));
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

	public Location getObserverSpawnLoc()
	{
		return _observerSpawnLocs != null ? _observerSpawnLocs.get(Rnd.get(_observerSpawnLocs.size())) : getSpawnLoc();
	}
}
