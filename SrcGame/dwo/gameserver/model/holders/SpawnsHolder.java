package dwo.gameserver.model.holders;

import java.util.ArrayList;
import java.util.List;

public class SpawnsHolder
{
	private final String _name;
	private final List<SpawnHolder> _sets;

	public SpawnsHolder(String name)
	{
		_sets = new ArrayList<>();
		_name = name;
	}

	public void addHolder(SpawnHolder holder)
	{
		_sets.add(holder);
	}

	public List<SpawnHolder> getHolders()
	{
		return _sets;
	}

	public void spawnAll()
	{
		for(SpawnHolder loc : _sets)
		{
			loc.doSpawn();
		}
	}

	public void spawnAll(int instanceId)
	{
		for(SpawnHolder loc : _sets)
		{
			loc.doSpawn(instanceId);
		}
	}

	public void unSpawnAll()
	{
		for(SpawnHolder loc : _sets)
		{
			loc.unSpawn();
		}
	}

	public void unSpawnAll(int instanceId)
	{
		for(SpawnHolder loc : _sets)
		{
			loc.unSpawn(instanceId);
		}
	}

	public String getName()
	{
		return _name;
	}
}
