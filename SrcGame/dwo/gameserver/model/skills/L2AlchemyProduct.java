package dwo.gameserver.model.skills;

/**
 * User: GenCloud
 * Date: 19.03.2015
 * Team: La2Era Team
 */
public class L2AlchemyProduct
{
    private final int _id;
    private final int _count;

    /**
     * @param id     crete item id
     * @param count    item count
     */
    public L2AlchemyProduct(int id, int count)
    {
        _id = id;
        _count = count;
    }

    public int getId()
    {
        return _id;
    }

    public int getMin()
    {
        return _count;
    }
}

