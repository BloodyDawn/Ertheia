package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionPlayerFlyMounted.
 * @author kerberos
 */

public class ConditionPlayerFlyMounted extends Condition
{
	private boolean _val;

	/**
	 * Instantiates a new condition player fly mounted.
	 * @param val the val
	 */
	public ConditionPlayerFlyMounted(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return !(env.getCharacter() instanceof L2PcInstance) || ((L2PcInstance) env.getCharacter()).isFlyingMounted() == _val;
	}
}