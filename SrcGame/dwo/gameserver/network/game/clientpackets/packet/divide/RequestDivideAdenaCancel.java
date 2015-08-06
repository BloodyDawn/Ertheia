package dwo.gameserver.network.game.clientpackets.packet.divide;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.divide.ExDivideAdenaCancel;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class RequestDivideAdenaCancel extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {
        //
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance _activeChar = getClient().getActiveChar();
        if(_activeChar != null && _activeChar.getParty() != null)
        {
            _activeChar.getParty().broadcastPacket(new ExShowScreenMessage("Распределение золота отменено", 5000)); //TODO уточнить мессагу, сделать красиво
            _activeChar.sendPacket(new ExDivideAdenaCancel());
        }
    }

    @Override
    public String getType()
    {
        return getClass().getSimpleName();
    }
}

