package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 19.03.12
 * Time: 9:23
 */

public class ConditionPlayerFame extends Condition
{
	private final int _fame;

	public ConditionPlayerFame(int fame)
	{
		_fame = fame;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getActingPlayer().getFame() >= _fame;
	}
}
