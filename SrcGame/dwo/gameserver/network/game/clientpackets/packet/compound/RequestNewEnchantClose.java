package dwo.gameserver.network.game.clientpackets.packet.compound;

import dwo.gameserver.model.actor.requests.CompoundRequest;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class RequestNewEnchantClose extends L2GameClientPacket
{

    @Override
    protected void readImpl()
    {
        //
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        {
            return;
        }

        activeChar.removeRequest(CompoundRequest.class);
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
