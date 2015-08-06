package dwo.gameserver.network.game.clientpackets.packet.divide;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.divide.ExDivideAdenaStart;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class RequestDivideAdenaStart extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null)
        {
            return;
        }

        //TODO Уточнить контент сообщений)
        if(activeChar.getParty() == null)
        {
            activeChar.sendMessage("Вы не состоите в группе");
        }
        else if(!activeChar.isPartyLeader())
        {
            activeChar.sendMessage("Вы не являетесь лидером группы");
        }
        else if (!activeChar.getParty().isInCommandChannel())
        {
            activeChar.getParty().broadcastPacket(new ExShowScreenMessage("Распределение золота начато", 5000));
            activeChar.sendPacket(new ExDivideAdenaStart());
        }
        else if (!activeChar.isCommandChannelLeader())
        {
            activeChar.sendMessage("Вы не являетесь лидером группы или главой командного канала!");
        }
        else if(activeChar.getParty().getCommandChannel().isLeader(activeChar))
        {
            activeChar.getParty().broadcastPacket(new ExShowScreenMessage("Распределение золота начато", 5000));
            activeChar.sendPacket(new ExDivideAdenaStart());
        }
        else
        {
            activeChar.sendMessage("Что-то пошло не так :(");
        }
    }

    @Override
    public String getType()
    {
        return "RequestDivideAdenaStart";
    }

}
