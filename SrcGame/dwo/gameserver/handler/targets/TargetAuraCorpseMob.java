package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import javolution.util.FastList;

import java.util.Collection;
import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetAuraCorpseMob implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		// Go through the L2Character _knownList
		Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
		for(L2Character obj : objs)
		{
			if(obj instanceof L2Attackable && obj.isDead())
			{
				if(onlyFirst)
				{
					return new L2Character[]{obj};
				}

				if(skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
				{
					break;
				}

				targetList.add(obj);
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_AURA_CORPSE_MOB;
	}
}
