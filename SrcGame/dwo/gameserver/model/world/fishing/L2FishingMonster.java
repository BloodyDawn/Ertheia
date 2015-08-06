package dwo.gameserver.model.world.fishing;

import dwo.gameserver.model.skills.stats.StatsSet;

public class L2FishingMonster
{
	private final int _userMinLevel;
	private final int _userMaxLevel;
	private final int _fishingMonsterId;
	private final int _probability;

	public L2FishingMonster(StatsSet set)
	{
		_userMinLevel = set.getInteger("userMinLevel");
		_userMaxLevel = set.getInteger("userMaxLevel");
		_fishingMonsterId = set.getInteger("fishingMonsterId");
		_probability = set.getInteger("probability");
	}

	/**
	 * @return the minimum user level.
	 */
	public int getUserMinLevel()
	{
		return _userMinLevel;
	}

	/**
	 * @return the maximum user level.
	 */
	public int getUserMaxLevel()
	{
		return _userMaxLevel;
	}

	/**
	 * @return the fishing monster Id.
	 */
	public int getFishingMonsterId()
	{
		return _fishingMonsterId;
	}

	/**
	 * @return the probability.
	 */
	public int getProbability()
	{
		return _probability;
	}
}