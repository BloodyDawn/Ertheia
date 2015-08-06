package dwo.gameserver.model.holders;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.stats.StatsSet;

public class ItemHolder
{
	private int _id;
	private long _count;
	private L2ItemInstance.ItemLocation _location = L2ItemInstance.ItemLocation.INVENTORY;
	private boolean _equipped;

	/**
	 * Стандартный холдер для предмета
	 *
	 * @param id ID предмета
	 * @param count количество предметов
	 */
	public ItemHolder(int id, long count)
	{
		_id = id;
		_count = count;
	}

	/**
	 * Специальный холдер для CharStartingItems
	 *
	 * @param set StatsSet
	 */
	public ItemHolder(StatsSet set)
	{
		_id = set.getInteger("id");
		_count = set.getInteger("count");
		_equipped = set.getBool("equipped", false);
		_location = L2ItemInstance.ItemLocation.valueOf(set.getString("location", "INVENTORY"));
	}

	/**
	 * @return ID предмета или Object ID
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return количество предмета
	 */
	public long getCount()
	{
		return _count;
	}

	public void setCount(long count)
	{
		_count = count;
	}

	/**
	 * @return надевать-ли предмет при получении игроком
	 */
	public boolean isEquipped()
	{
		return _equipped;
	}

	public void setEquipped(boolean equipped)
	{
		_equipped = equipped;
	}

	public L2ItemInstance.ItemLocation getItemLocation()
	{
		return _location;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": Id: " + _id + " Count: " + _count;
	}
}
