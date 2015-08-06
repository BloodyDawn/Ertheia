package dwo.gameserver.network.game.serverpackets.packet.compound;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class ExEnchantSucess extends L2GameServerPacket
{
    private final int _itemId;

    public ExEnchantSucess(int itemId)
    {
        _itemId = itemId;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_itemId);
    }
}
