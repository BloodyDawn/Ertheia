package dwo.gameserver.model.world.fishing;

import dwo.gameserver.model.skills.stats.StatsSet;

public class L2FishingRod
{
	private final int _fishingRodId;
	private final int _fishingRodItemId;
	private final int _fishingRodLevel;
	private final String _fishingRodName;
	private final double _fishingRodDamage;

	public L2FishingRod(StatsSet set)
	{
		_fishingRodId = set.getInteger("fishingRodId");
		_fishingRodItemId = set.getInteger("fishingRodItemId");
		_fishingRodLevel = set.getInteger("fishingRodLevel");
		_fishingRodName = set.getString("fishingRodName");
		_fishingRodDamage = set.getDouble("fishingRodDamage");
	}

	/**
	 * @return the fishing rod Id.
	 */
	public int getFishingRodId()
	{
		return _fishingRodId;
	}

	/**
	 * @return the fishing rod Item Id.
	 */
	public int getFishingRodItemId()
	{
		return _fishingRodItemId;
	}

	/**
	 * @return the fishing rod Level.
	 */
	public int getFishingRodLevel()
	{
		return _fishingRodLevel;
	}

	/**
	 * @return the fishing rod Item Name.
	 */
	public String getFishingRodItemName()
	{
		return _fishingRodName;
	}

	/**
	 * @return the fishing rod Damage.
	 */
	public double getFishingRodDamage()
	{
		return _fishingRodDamage;
	}
}