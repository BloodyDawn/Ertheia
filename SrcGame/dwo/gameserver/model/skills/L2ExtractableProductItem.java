package dwo.gameserver.model.skills;

import dwo.gameserver.model.holders.ItemHolder;

import java.util.List;

public class L2ExtractableProductItem
{
	private final List<ItemHolder> _items;
	private final double _chance;

	public L2ExtractableProductItem(List<ItemHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}

	/**
	 * @return the production list.
	 */
	public List<ItemHolder> getItems()
	{
		return _items;
	}

	/**
	 * @return the chance of the production list.
	 */
	public double getChance()
	{
		return _chance;
	}
}
