package dwo.gameserver.model.actor.requests;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class CompoundRequest extends AbstractRequest
{
    private int _itemOne;
    private int _itemTwo;

    public CompoundRequest(L2PcInstance activeChar)
    {
        super(activeChar);
    }

    public L2ItemInstance getItemOne()
    {
        return getActiveChar().getInventory().getItemByObjectId(_itemOne);
    }

    public void setItemOne(int itemOne)
    {
        _itemOne = itemOne;
    }

    public L2ItemInstance getItemTwo()
    {
        return getActiveChar().getInventory().getItemByObjectId(_itemTwo);
    }

    public void setItemTwo(int itemTwo)
    {
        _itemTwo = itemTwo;
    }

    @Override
    public boolean isItemRequest()
    {
        return true;
    }

    @Override
    public boolean canWorkWith(AbstractRequest request)
    {
        return !request.isItemRequest();
    }

    @Override
    public boolean isUsing(int objectId)
    {
        return (objectId > 0) && ((objectId == _itemOne) || (objectId == _itemTwo));
    }
}
