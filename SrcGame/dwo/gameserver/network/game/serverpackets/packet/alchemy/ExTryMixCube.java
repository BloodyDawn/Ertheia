package dwo.gameserver.network.game.serverpackets.packet.alchemy;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 21.01.2015
 * Team: La2Era Team
 * ??? TODO
 */
public class ExTryMixCube extends L2GameServerPacket
{
    private int _stoneID;
    private long _count;
    private int _typePacket;

    public static final L2GameServerPacket FAIL = new ExTryMixCube(6);

    public ExTryMixCube(int typePacket) {
        _typePacket = typePacket;
        _stoneID = 0;
        _count = 0;
    }
    
    public ExTryMixCube(long count, int stoneId)
    {
        _typePacket = 0;
        _count = count;
        _stoneID = stoneId;
    }

    @Override
    protected void writeImpl()
    {
        writeC(_typePacket); //unk
        writeC(0x01); //type
        writeD(0x00); //unk
        writeD(_stoneID); //stoneID
        writeQ(_count); //count
    }
}
