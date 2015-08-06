package dwo.gameserver.network.game.serverpackets.packet.alchemy;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class ExAlchemySkillList extends L2GameServerPacket
{
    private final List<L2Skill> _skills = new ArrayList<>();

    public ExAlchemySkillList(L2PcInstance player)
    {
        _skills.addAll(player.getAllSkills().stream().filter(skill -> SkillTreesData.getInstance().getAlchemySkill(skill.getId(), skill.getLevel()) != null).map(skill -> skill).collect(Collectors.toList()));
    }


    @Override
    protected void writeImpl()
    {
        writeD(_skills.size());
        for (L2Skill skill : _skills)
        {
            writeD(skill.getId());
            writeD(skill.getLevel());
            writeD(0x00); //Всегла 0 (Евроофф)i
            writeD(0x00);
            writeC(skill.getId() != 17943 ? 1 : 0);
        }
    }
}
