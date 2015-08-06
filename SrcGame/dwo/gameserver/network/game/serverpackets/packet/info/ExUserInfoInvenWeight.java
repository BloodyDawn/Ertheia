package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExUserInfoInvenWeight extends L2GameServerPacket
{
    private L2PcInstance _activeChar;

    public ExUserInfoInvenWeight(L2PcInstance character)
    {
        _activeChar = character;
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_activeChar.getObjectId());
        writeD(_activeChar.getCurrentLoad());
        writeD(_activeChar.getMaxLoad());
    }
}