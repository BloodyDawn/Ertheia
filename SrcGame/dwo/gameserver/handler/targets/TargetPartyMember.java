package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;

public class TargetPartyMember implements ITargetTypeHandler
{
    @Override
    public L2Object[] getTargetList(final L2Skill skill, final L2Character activeChar, final boolean onlyFirst, final L2Character target)
    {
        if (target == null)
        {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return _emptyTargetList;
        }

        if (skill.getSkillType() != L2SkillType.RESURRECT && target.isDead())
        {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            return _emptyTargetList;
        }

        if (target == activeChar)
        {
            return new L2Character[]{target};
        }

        if (activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId())
        {
            return new L2Character[]{target};
        }

        if (activeChar.isPlayer() && target.isSummon() && activeChar.getPets().contains(target))
        {
            return new L2Character[]{target};
        }

        if (activeChar.isSummon() && target.isPlayer() && target.getPets().contains(activeChar))
        {
            return new L2Character[]{target};
        }

        return _emptyTargetList;
    }

    @Override
    public Enum<L2TargetType> getTargetType()
    {
        return L2TargetType.TARGET_PARTY_MEMBER;
    }
}
