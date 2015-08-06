package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

public class ConditionPlayerCanTransform extends Condition
{
	private final boolean _val;

	public ConditionPlayerCanTransform(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) env.getCharacter();

		return _val == (activeChar.getTransformationId() == 0 &&
			!activeChar.isAlikeDead() &&
			!activeChar.isCursedWeaponEquipped() &&
			!activeChar.isMounted() &&
			!activeChar.isFlyingMounted());
	}
}