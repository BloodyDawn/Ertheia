package dwo.gameserver.handler.targets;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import javolution.util.FastList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TargetChain implements ITargetTypeHandler
{
	private static final Comparator<L2Character> SORT_COMPARATOR = (o1, o2) -> Double.compare(o1.getCurrentHp() / o1.getMaxHp(), o2.getCurrentHp() / o2.getMaxHp());

	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if(activeChar.isPlayer())
		{
			L2Character primarytarget = null;
			if(activeChar.getTarget() instanceof L2Character)
			{
				primarytarget = (L2Character) activeChar.getTarget();
				if(primarytarget.equals(activeChar))
				{
					targetList.add(activeChar);
				}
				else if(GeoEngine.getInstance().canSeeTarget(activeChar, primarytarget) && L2Skill.checkForAreaFriendlySkills(activeChar.getActingPlayer(), primarytarget, skill))
				{
					targetList.add(primarytarget);
				}
			}

			if(targetList.isEmpty()) // primary target must be in
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return _emptyTargetList;
			}

			// target skill radius
			for(L2Character o : primarytarget.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
			{
				// chain heal does not affects caster unless he is targeting himself
				// even if on range
				if(o.equals(activeChar))
				{
					continue;
				}
				if(!GeoEngine.getInstance().canSeeTarget(primarytarget, o))
				{
					continue;
				}
				if(L2Skill.checkForAreaFriendlySkills(activeChar.getActingPlayer(), o, skill))
				{
					targetList.add(o);
				}
			}

			if(targetList.size() <= skill.getMaxTargets())
			{
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			else
			{
				Collections.sort(targetList, SORT_COMPARATOR);
				if(targetList.size() > skill.getMaxTargets())
				{
					targetList = targetList.subList(0, skill.getMaxTargets());
					// Выставляем превичную цель на первое место в листе
					if(!targetList.contains(primarytarget))
					{
						targetList.set(skill.getMaxTargets() - 1, targetList.get(0));
						targetList.set(0, primarytarget);
					}
					else if(!targetList.get(0).equals(primarytarget))
					{
						int pos;
						for(pos = 1; pos < skill.getMaxTargets(); pos++)
						{
							if(targetList.get(pos).equals(primarytarget))
							{
								break;
							}
						}
						targetList.set(pos, targetList.get(0));
						targetList.set(0, primarytarget);
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
		}
		else
		{
			return _emptyTargetList;
		}
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CHAIN;
	}
}
