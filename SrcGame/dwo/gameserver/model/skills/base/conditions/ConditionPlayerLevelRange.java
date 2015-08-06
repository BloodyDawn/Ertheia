package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

public class ConditionPlayerLevelRange extends Condition
{
	private final int[] _levels;

	/**
	 * Instantiates a new condition player levels range.
	 * @param levels {@code levels} range.
	 */
	public ConditionPlayerLevelRange(int[] levels)
	{
		_levels = levels;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getLevel() >= _levels[0] && env.getCharacter().getLevel() <= _levels[1];
	}
}