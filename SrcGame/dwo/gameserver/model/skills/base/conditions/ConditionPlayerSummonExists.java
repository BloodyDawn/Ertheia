package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 15.01.12
 * Time: 21:34
 */

public class ConditionPlayerSummonExists extends Condition
{
	private final boolean _val;

	public ConditionPlayerSummonExists(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return _val == !env.getCharacter().getPets().isEmpty();
	}
}