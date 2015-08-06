package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.12.12
 * Time: 0:35
 */
public class ConditionClanLevel extends Condition
{
	private final int _level;

	public ConditionClanLevel(int level)
	{
		_level = level;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getActingPlayer().getClan() != null && env.getCharacter().getActingPlayer().getClan().getLevel() >= _level;
	}
}
