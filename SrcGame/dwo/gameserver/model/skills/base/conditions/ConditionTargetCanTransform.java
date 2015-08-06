package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.09.12
 * Time: 19:53
 */

public class ConditionTargetCanTransform extends Condition
{
	private final boolean _val;

	public ConditionTargetCanTransform(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getTarget() == null)
		{
			return false;
		}
		if(!(env.getTarget() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance targetChar = (L2PcInstance) env.getTarget();

		return _val == (targetChar.getTransformationId() == 0 &&
			!targetChar.isAlikeDead() &&
			!targetChar.isCursedWeaponEquipped() &&
			!targetChar.isMounted() &&
			!targetChar.isFlyingMounted());
	}
}