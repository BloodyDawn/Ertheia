package dwo.gameserver.network.game.serverpackets.packet.ability;

import dwo.gameserver.datatables.xml.AbilityPointsData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExAcquireAPSkillList extends L2GameServerPacket
{
    private final int _abilityPoints, _usedAbilityPoints;
    private final long _price;
    private final boolean _enable;
    private final List<L2Skill> _skills = new ArrayList<>();

    public ExAcquireAPSkillList(L2PcInstance activeChar)
    {
        _abilityPoints = activeChar.getAbilityPoints();
        _usedAbilityPoints = activeChar.getAbilityPointsUsed();
        _price = AbilityPointsData.getInstance().getPrice(_abilityPoints);
        for (L2SkillLearn sk : SkillTreesData.getInstance().getAbilitySkillTree().values())
        {
            final L2Skill knownSkill = activeChar.getKnownSkill(sk.getSkillId());
            if (knownSkill != null)
            {
                if (knownSkill.getLevel() == sk.getSkillLevel())
                {
                    _skills.add(knownSkill);
                }
            }
        }
        _enable = !activeChar.isSubClassActive() && (activeChar.getLevel() >= 99) && activeChar.isNoble();
    }

    @Override
    protected void writeImpl()
    {
        writeD(_enable ? 1 : 0);
        writeQ(10000000L);
        writeQ(_price);
        writeD(16);
        writeD(_abilityPoints);
        writeD(_usedAbilityPoints);
        writeD(_skills.size());
        for (L2Skill skill : _skills)
        {
            writeD(skill.getId());
            writeD(skill.getLevel());
        }
    }
}
