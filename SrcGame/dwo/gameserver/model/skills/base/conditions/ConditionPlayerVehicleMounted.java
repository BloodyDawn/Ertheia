package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author Nyaran
 */
public class ConditionPlayerVehicleMounted extends Condition
{
	private boolean _val;

	/**
	 * @param val the val
	 */
	public ConditionPlayerVehicleMounted(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return !(env.getCharacter() instanceof L2PcInstance) || ((L2PcInstance) env.getCharacter()).isInVehicle() == _val;
	}
}