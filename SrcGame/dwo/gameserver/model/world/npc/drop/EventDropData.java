package dwo.gameserver.model.world.npc.drop;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 04.04.12
 * Time: 0:27
 */

public class EventDropData
{
	private final int _itemId;
	private final long _minCount;
	private final long _maxCount;
	private final int _minLevel;
	private final int _maxLevel;
	private final double _chance;

	public EventDropData(int itemId, int minCount, int maxCount, int minLevel, int maxLevel, double chance)
	{
		_itemId = itemId;
		_minCount = minCount;
		_maxCount = maxCount;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_chance = chance;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getMinCount()
	{
		return _minCount;
	}

	public long getMaxCount()
	{
		return _maxCount;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public double getChance()
	{
		return _chance;
	}
}
