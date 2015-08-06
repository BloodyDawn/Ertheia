package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExUserInfoCubic extends L2GameServerPacket
{
    private L2PcInstance _activeChar;

    public ExUserInfoCubic(L2PcInstance character)
    {
        _activeChar = character;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_activeChar.getObjectId());
        writeH(_activeChar.getCubics().size());
        for(L2CubicInstance cubic : _activeChar.getCubics())
        {
            writeH(cubic.getId());
        }
        writeD(_activeChar.getAgathionId());
    }
}
