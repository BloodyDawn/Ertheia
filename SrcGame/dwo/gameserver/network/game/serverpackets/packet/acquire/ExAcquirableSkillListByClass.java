package dwo.gameserver.network.game.serverpackets.packet.acquire;

import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

/**
 * L2GOD Team
 * User: Keiichi, Bacek, GenCloud
 * Date: 08.05.2012
 * Type: Glory Days
 */

public class ExAcquirableSkillListByClass extends L2GameServerPacket
{
    private FastList<Skill> _skills = new FastList<>();
    private AcquireSkillType _skillType;

    public ExAcquirableSkillListByClass(AcquireSkillType type)
    {
        _skillType = type;
    }

    public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
    {
        _skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
    }

    public int getSkillCount()
    {
        return _skills.size();
    }

    @Override
    protected void writeImpl()
    {
        if(_skills.isEmpty())
        {
            return;
        }

        writeH(_skillType.getId());
        writeH(_skills.size());

        for(Skill temp : _skills)
        {
            writeD(temp.id);
            writeH(temp.nextLevel);
            writeH(temp.maxLevel);
            writeC(temp.requirements);
            writeQ(temp.spCost);
            writeC(0x01);
            if(_skillType == AcquireSkillType.SubPledge)
            {
                writeD(0x00); //TODO: ?
            }
        }
    }

    /**
     * Private class containing learning skill information.
     */
    private static class Skill
    {
        public int id;
        public int nextLevel;
        public int maxLevel;
        public int spCost;
        public int requirements;

        public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
        {
            id = pId;
            nextLevel = pNextLevel;
            maxLevel = pMaxLevel;
            spCost = pSpCost;
            requirements = pRequirements;
        }
    }
}