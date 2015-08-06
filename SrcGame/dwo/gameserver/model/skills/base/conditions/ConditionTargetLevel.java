package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionTargetLevel.
 *
 * @author mkizub
 */
public class ConditionTargetLevel extends Condition
{
	private final int _level;

	/**
	 * Instantiates a new condition target level.
	 *
	 * @param level the level
	 */
	public ConditionTargetLevel(int level)
	{
		_level = level;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getTarget() == null)
		{
			return false;
		}
		return env.getTarget().getLevel() >= _level;
	}
}
