package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.11.11
 * Time: 3:07
 */

public class ConditionPlayerAwakened extends Condition
{
	private final boolean _val;

	/**
	 * @param val the val
	 */
	public ConditionPlayerAwakened(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return !(env.getCharacter() instanceof L2PcInstance) || env.getCharacter().isAwakened() == _val;
	}
}
