package dwo.gameserver.network.game.clientpackets.packet.alchemy;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.alchemy.ExAlchemySkillList;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class RequestAlchemySkillList extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {

    }

    @Override
    protected void runImpl()
    {
        final L2PcInstance activeChar = getClient().getActiveChar();
        if ((activeChar == null) || (activeChar.getRace() != Race.Ertheia))
        {
            return;
        }
        activeChar.sendPacket(new ExAlchemySkillList(activeChar));
    }

    @Override
    public String getType()
    {
        return getClass().getSimpleName();
    }
}
