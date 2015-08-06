package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Collection;
import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetAreaSummon implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		int radius = 0;
		boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
		Collection<L2Character> objs = null;
		if(!activeChar.getPets().isEmpty())
		{
			for(L2Summon pet : activeChar.getPets())
			{
				if(pet == null || !(pet instanceof L2SummonInstance) || pet.isDead())
				{
					return _emptyTargetList;
				}

				if(onlyFirst)
				{
					return new L2Character[]{pet};
				}

				objs = pet.getKnownList().getKnownCharacters();
				radius = skill.getSkillRadius();

				for(L2Character obj : objs)
				{
					if(obj == null || obj.equals(target) || obj.equals(activeChar))
					{
						continue;
					}

					if(!Util.checkIfInRange(radius, pet, obj, true))
					{
						continue;
					}

					if(!(obj instanceof L2Attackable || obj instanceof L2Playable))
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
		return L2TargetType.TARGET_AREA_SUMMON;
	}
}
