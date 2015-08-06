package dwo.gameserver.network.game.serverpackets.packet.acquire;

import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

/**
 *  Проверил: GenCLoud
 *  Дата: 17.01.15
 *  Протокол: 603 ( Ertheia )
 */
public class AcquireSkillInfo extends L2GameServerPacket
{
    private FastList<Req> _reqs;
    private int _id;
    private int _level;
    private int _spCost;
    private AcquireSkillType _type;

    public AcquireSkillInfo(int id, int level, int spCost, AcquireSkillType type)
    {
        _reqs = new FastList<>();
        _id = id;
        _level = level;
        _spCost = spCost;
        _type = type;
    }

    public void addRequirement(int type, int id, int count, int unk)
    {
        _reqs.add(new Req(type, id, count, unk));
    }

    @Override
    protected void writeImpl()
    {
        writeD(_id);
        writeD(_level);
        writeQ(_spCost);
        writeD(_type.getId());
        writeD(_reqs.size());
        for(Req temp : _reqs)
        {
            writeD(temp.type);
            writeD(temp.itemId);
            writeQ(temp.count);
            writeD(temp.unk);
        }
    }

    /**
     * Private class containing learning skill requisites.
     */
    private static class Req
    {
        public int itemId;
        public int count;
        public int type;
        public int unk;

        public Req(int pType, int pItemId, int pCount, int pUnk)
        {
            itemId = pItemId;
            type = pType;
            count = pCount;
            unk = pUnk;
        }
    }
}
