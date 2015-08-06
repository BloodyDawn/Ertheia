package dwo.gameserver.network.game.serverpackets.packet.ability;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExCloseAPListWnd extends L2GameServerPacket
{
    public static ExCloseAPListWnd STATIC_PACKET = new ExCloseAPListWnd();

    private ExCloseAPListWnd()
    {
    }

    @Override
    protected void writeImpl()
    {
    }
}
