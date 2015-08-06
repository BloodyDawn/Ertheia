package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.06.12
 * Time: 0:59
 */

public class TargetSummonOne implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if(target == null || target.isDead() || !(target instanceof L2SummonInstance))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}
		return new L2Character[]{target};
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_SUMMON_ONE;
	}
}
