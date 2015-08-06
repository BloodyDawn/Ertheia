package dwo.gameserver.model.holders;

import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.fort.FortFacilityType;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.13
 * Time: 18:46
 */
public class FortFacilitySpawnHolder
{
	private FortFacilityType _type;
	private int _level;
	private List<L2Spawn> _spawnList;

	public FortFacilitySpawnHolder(FortFacilityType type, int level, List<L2Spawn> spawnList)
	{
		_type = type;
		_level = level;
		_spawnList = spawnList;
	}

	public FortFacilityType getType()
	{
		return _type;
	}

	public int getLevel()
	{
		return _level;
	}

	public List<L2Spawn> getSpawnList()
	{
		return _spawnList;
	}
}