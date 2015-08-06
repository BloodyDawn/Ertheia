package dwo.gameserver.model.actor.requests;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * User: GenCloud
 * Date: 29.03.2015
 * Team: La2Era Team
 */
public class PrimeShopRequest extends AbstractRequest
{
    public PrimeShopRequest(L2PcInstance activeChar)
    {
        super(activeChar);
    }

    @Override
    public boolean isUsing(int objectId)
    {
        return false;
    }
}
