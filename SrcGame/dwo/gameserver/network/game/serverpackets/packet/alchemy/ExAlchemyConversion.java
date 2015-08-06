package dwo.gameserver.network.game.serverpackets.packet.alchemy;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 20.01.2015
 * Team: La2Era Team
 */
public class ExAlchemyConversion extends L2GameServerPacket 
{
    private final int _result;
    private final int _successCount;
    private final int _failCount;

    public static final L2GameServerPacket FAIL;

    public ExAlchemyConversion(int result)
    {
        _result = result;
        _successCount = 0;
        _failCount = 0;
    }

    public ExAlchemyConversion(int successCount, int failCount) {
        _result = 0;
        _successCount = successCount;
        _failCount = failCount;
    }

    @Override
    protected void writeImpl() 
    {
        writeC(_result);
        writeD(_successCount);
        writeD(_failCount);
    }

    static {
        FAIL = new ExAlchemyConversion(-1);
    }
}
