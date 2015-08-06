package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author UnAfraid
 */

public class TargetAreaCorpseMob implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if(!(target instanceof L2Attackable) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}

		if(onlyFirst)
		{
			return new L2Character[]{target};
		}

		targetList.add(target);

		boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);

		int radius = skill.getSkillRadius();
		Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
		for(L2Character obj : objs)
		{
			if(!(obj instanceof L2Attackable || obj instanceof L2Playable) || !Util.checkIfInRange(radius, target, obj, true))
			{
				continue;
			}

			if(!L2Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
			{
				continue;
			}

			targetList.add(obj);
		}

		if(targetList.isEmpty())
		{
			return _emptyTargetList;
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_AREA_CORPSE_MOB;
	}
}