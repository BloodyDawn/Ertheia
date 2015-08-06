package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author UnAfraid
 */

public class TargetOne implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		boolean canTargetSelf = false;
		switch(skill.getSkillType())
		{
			case BUFF:
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case MANARECHARGE:
			case MANA_BY_LEVEL:
			case MANAHEAL:
			case NEGATE:
			case CANCEL_DEBUFF:
			case COMBATPOINTHEAL:
			case BALANCE_LIFE:
			case HPMPCPHEAL_PERCENT:
			case HPMPHEAL_PERCENT:
			case HPCPHEAL_PERCENT:
			case DUMMY:
			case HEAL_COHERENTLY:
			case SUMMON: // Для скилла Целителя Альгизы, т.к. при TARGET_ONE на себя тоже повесить можно.
				canTargetSelf = true;
				break;
		}

		// Check for null target or any other invalid target
		if((target == null || target.isDead() || target.equals(activeChar) && !canTargetSelf) && !skill.isTriggeredSkill())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}

		// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
		return new L2Character[]{target};
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_ONE;
	}
}
