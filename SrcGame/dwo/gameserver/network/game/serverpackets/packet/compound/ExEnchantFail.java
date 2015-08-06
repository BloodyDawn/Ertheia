package dwo.gameserver.network.game.serverpackets.packet.compound;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class ExEnchantFail extends L2GameServerPacket
{
    public static final ExEnchantFail STATIC_PACKET = new ExEnchantFail(0, 0);
    private final int _itemOne;
    private final int _itemTwo;

    public ExEnchantFail(int itemOne, int itemTwo)
    {
        _itemOne = itemOne;
        _itemTwo = itemTwo;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_itemOne);
        writeD(_itemTwo);
    }
}
