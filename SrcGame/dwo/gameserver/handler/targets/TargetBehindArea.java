package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.world.zone.TargetPosition;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Collection;
import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetBehindArea implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if((target == null || target.equals(activeChar) || target.isAlikeDead()) && skill.getCastRange() >= 0 || !(target instanceof L2Attackable || target instanceof L2Playable))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return _emptyTargetList;
		}

		L2Character origin;
		boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
		int radius = skill.getSkillRadius();

		if(skill.getCastRange() >= 0)
		{
			if(!L2Skill.checkForAreaOffensiveSkills(activeChar, target, skill, srcInArena))
			{
				return _emptyTargetList;
			}

			if(onlyFirst)
			{
				return new L2Character[]{target};
			}

			origin = target;
			targetList.add(origin); // Add target to target list
		}
		else
		{
			origin = activeChar;
		}

		Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
		for(L2Character obj : objs)
		{
			if(!(obj instanceof L2Attackable || obj instanceof L2Playable))
			{
				continue;
			}

			if(obj.equals(origin))
			{
				continue;
			}

			if(Util.checkIfInRange(radius, origin, obj, true))
			{
				if(obj.getTargetPosition(target) != TargetPosition.BACK)
				{
					continue;
				}

				if(!L2Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
				{
					continue;
				}

				if(skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
				{
					break;
				}

				targetList.add(obj);
			}
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
		return L2TargetType.TARGET_BEHIND_AREA;
	}
}
