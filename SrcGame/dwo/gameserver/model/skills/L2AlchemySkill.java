package dwo.gameserver.model.skills;

import java.util.List;

/**
 * User: GenCloud
 * Date: 19.03.2015
 * Team: La2Era Team
 */
public class L2AlchemySkill 
{
    private final int _hash;
    private final List<L2AlchemyProductItem> _product;

    public L2AlchemySkill(int hash, List<L2AlchemyProductItem> products)
    {
        _hash = hash;
        _product = products;
    }

    public int getSkillHash()
    {
        return _hash;
    }

    public List<L2AlchemyProductItem> getProductItems()
    {
        return _product;
    }
}
