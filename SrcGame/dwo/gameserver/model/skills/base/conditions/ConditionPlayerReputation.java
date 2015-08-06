package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.03.12
 * Time: 1:46
 */

public class ConditionPlayerReputation extends Condition
{
	private final int _reputation;

	public ConditionPlayerReputation(int reputation)
	{
		_reputation = reputation;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getActingPlayer().getReputation() >= _reputation;
	}
}
