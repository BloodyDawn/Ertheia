package dwo.gameserver.model.items.soulcrystal;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.06.12
 * Time: 17:47
 */

public class SoulCrystal
{
	private final int _level;
	private final int _itemId;
	private final int _leveledItemId;

	public SoulCrystal(int level, int itemId, int leveledItemId)
	{
		_level = level;
		_itemId = itemId;
		_leveledItemId = leveledItemId;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getLeveledItemId()
	{
		return _leveledItemId;
	}
}