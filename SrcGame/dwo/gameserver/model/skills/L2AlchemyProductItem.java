package dwo.gameserver.model.skills;

import dwo.gameserver.model.holders.ItemHolder;

import java.util.List;

/**
 * User: GenCloud
 * Date: 19.03.2015
 * Team: La2Era Team
 */
public class L2AlchemyProductItem
{
    private final List<ItemHolder> _items;

    public L2AlchemyProductItem(List<ItemHolder> items)
    {
        _items = items;
    }

    public List<ItemHolder> getItems()
    {
        return _items;
    }
}
