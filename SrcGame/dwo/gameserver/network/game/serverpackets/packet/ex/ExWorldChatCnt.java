package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class ExWorldChatCnt extends L2GameServerPacket
{
    private int _points;
    private SystemMessageId _sysId;
    public boolean _sysChat = false;

    public ExWorldChatCnt(L2PcInstance activeChar)
    {
        _sysChat = false;
        _points = activeChar.getWorldChatPoints();
    }
    
    public ExWorldChatCnt(SystemMessageId sysId)
    {
        _sysChat = true;
        _sysId = sysId;        
    }

    @Override
    protected void writeImpl()
    {
        if (_sysChat) 
        {
            writeS(String.valueOf(_sysId));
        }
        else
        {
            writeD(_points);
        }
    }
}
