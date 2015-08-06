package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionPlayerIsClanLeader.
 */

public class ConditionPlayerIsClanLeader extends Condition
{
	private final boolean _val;

	/**
	 * Instantiates a new condition player is clan leader.
	 *
	 * @param val the val
	 */
	public ConditionPlayerIsClanLeader(boolean val)
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
		return ((L2PcInstance) env.getCharacter()).isClanLeader() == _val;
	}
}
