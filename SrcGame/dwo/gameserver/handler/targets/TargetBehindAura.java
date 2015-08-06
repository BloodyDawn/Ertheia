package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.world.zone.TargetPosition;
import javolution.util.FastList;

import java.util.Collection;
import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetBehindAura implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);

		L2PcInstance sourcePlayer = activeChar.getActingPlayer();

		Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());

		if(skill.getSkillType() == L2SkillType.DUMMY)
		{
			if(onlyFirst)
			{
				return new L2Character[]{activeChar};
			}

			targetList.add(activeChar);
			for(L2Character obj : objs)
			{
				if(!(obj.equals(activeChar) || obj.equals(sourcePlayer) || obj instanceof L2Npc || obj instanceof L2Attackable))
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
		else
		{
			for(L2Character obj : objs)
			{
				if(obj instanceof L2Attackable || obj instanceof L2Playable)
				{
					if(obj.getTargetPosition(activeChar) != TargetPosition.BACK)
					{
						continue;
					}

					if(!L2Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
					{
						continue;
					}

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
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_BEHIND_AURA;
	}
}