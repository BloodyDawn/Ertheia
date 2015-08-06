package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

public class ConditionTargetLevelRange extends Condition
{
	private final int[] _levels;

	/**
	 * Instantiates a new condition target levels range.
	 * @param levels the {@code levels} range.
	 */
	public ConditionTargetLevelRange(int[] levels)
	{
		_levels = levels;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getTarget() == null)
		{
			return false;
		}
		int level = env.getTarget().getLevel();
		return level >= _levels[0] && level <= _levels[1];
	}
}
