package dwo.gameserver.handler.targets;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.11.11
 * Time: 2:02
 */

public class TargetSublime implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if(activeChar instanceof L2PcInstance)
		{
			for(L2Character o : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
			{
				if(o.equals(activeChar))
				{
					continue;
				}

				if(!GeoEngine.getInstance().canSeeTarget(activeChar, o))
				{
					continue;
				}

				if(L2Skill.checkForAreaFriendlySkills((L2PcInstance) activeChar, o, skill))
				{
					targetList.add(o);
				}
			}
		}
		else
		{
			return _emptyTargetList;
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_SUBLIME;
	}
}
