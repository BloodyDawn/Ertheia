package dwo.gameserver.network.game.serverpackets.packet.acquire;

import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * La2Era Team
 * User: GenCloud
 * Date: 17.01.2015
 * Type: Ertheia
 */

public class ExAcquireSkillInfo extends L2GameServerPacket
{
    private List<ItemHolder> _items;
    private List<SkillHolder> _skills;
    private int _id;
    private int _level;
    private int _spCost;
    private int _mode;

    public ExAcquireSkillInfo(int id, int level, int spCost, int mode, List<SkillHolder> skills, List<ItemHolder> items)
    {
        _id = id;
        _level = level;
        _spCost = spCost;
        _mode = mode;
        _items = items;
        _skills = skills;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_id);
        writeD(_level);
        writeQ(_spCost);
        writeH(_level + 1);
        writeH(0x00);
        writeD(_items.size());
        for(ItemHolder item : _items)
        {
            writeD(item.getId());
            writeQ(item.getCount());
        }
        writeD(_skills.size());
        for(SkillHolder skill : _skills)
        {
            writeD(skill.getSkillId());
            writeD(skill.getSkillLvl());
        }
    }
}