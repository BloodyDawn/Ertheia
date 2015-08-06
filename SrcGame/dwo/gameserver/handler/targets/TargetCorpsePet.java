package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author UnAfraid
 *
 */
public class TargetCorpsePet implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if(activeChar instanceof L2PcInstance)
		{
			if(!activeChar.getPets().isEmpty())
			{
				targetList.addAll(activeChar.getPets().stream().filter(pet -> pet != null && pet.isDead()).collect(Collectors.toList()));
				return targetList.toArray(new L2Character[targetList.size()]);
			}
		}
		return _emptyTargetList;
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CORPSE_PET;
	}
}
