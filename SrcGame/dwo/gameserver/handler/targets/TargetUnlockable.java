package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2ChestInstance;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2FireChestInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import javolution.util.FastList;

import java.util.List;

public class TargetUnlockable implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if(!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance) && !(target instanceof L2FireChestInstance))
		{
			//activeChar.sendPacket(SystemMessage.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}

		if(onlyFirst)
		{
			return new L2Character[]{target};
		}
		else
		{
			targetList.add(target);
			return targetList.toArray(new L2Character[targetList.size()]);
		}
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_UNLOCKABLE;
	}
}
