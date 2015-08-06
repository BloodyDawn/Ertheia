package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author DS
 */

public class ConditionTargetPlayable extends Condition
{
	@Override
	public boolean testImpl(Env env)
	{
		return env.getTarget() instanceof L2Playable;
	}
}