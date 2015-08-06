package dwo.gameserver.handler.targets;

import dwo.config.Config;
import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import javolution.util.FastList;

import java.util.List;

/**
 * @author UnAfraid
 */

public class TargetCorpseMob implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		boolean isSummon = target instanceof L2SummonInstance;
		if(!(isSummon || target instanceof L2Attackable) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}

		// Corpse mob only available for half time
		switch(skill.getSkillType())
		{
			case SUMMON:
				if(isSummon && ((L2SummonInstance) target).getOwner() != null && ((L2SummonInstance) target).getOwner().getObjectId() == activeChar.getObjectId())
				{
					return _emptyTargetList;
				}
				break;
			case DRAIN:
				if(target instanceof L2Attackable && !((L2Attackable) target).checkCorpseTime(activeChar.getActingPlayer(), Config.NPC_DECAY_TIME / 2, true))
				{
					return _emptyTargetList;
				}
		}

		if(!onlyFirst)
		{
			targetList.add(target);
			return targetList.toArray(new L2Object[targetList.size()]);
		}
		return new L2Character[]{target};
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CORPSE_MOB;
	}
}