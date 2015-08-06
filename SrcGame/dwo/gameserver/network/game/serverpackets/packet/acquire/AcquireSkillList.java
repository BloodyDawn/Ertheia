package dwo.gameserver.network.game.serverpackets.packet.acquire;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * @author : GenCloud
 * @version 1.5
 */
public class AcquireSkillList extends L2GameServerPacket
{
    private FastList<L2SkillLearn> _skills;
    private L2PcInstance _player;

    public AcquireSkillList(L2PcInstance player)
    {
        _skills = SkillTreesData.getInstance().getAvailableSkills(player, player.getClassId(), true, false, true);
        _player = player;
    }

    @Override
    protected void writeImpl()
    {
        writeH(_skills.size());
        for(L2SkillLearn sk : _skills)
        {
            writeD(sk.getSkillId());
            writeH(sk.getSkillLevel());
            writeQ(sk.getLevelUpSp());
            writeC(sk.getMinLevel());
            writeC(sk.getMinDualLevel());

            writeC(sk.getRequiredItems().size());
            for(ItemHolder item : sk.getRequiredItems())
            {
                writeD(item.getId());
                writeQ(item.getCount());
            }

            List<SkillHolder> s = sk.getPrequisiteSkills(_player, true);
            writeC(s.size());
            for(SkillHolder skill : s)
            {
                writeD(skill.getSkillId());
                writeH(skill.getSkillLvl());
            }
        }
        FastList.recycle(_skills);
    }
}
