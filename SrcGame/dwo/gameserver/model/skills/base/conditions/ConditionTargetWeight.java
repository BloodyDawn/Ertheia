package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

public class ConditionTargetWeight extends Condition
{
	private final int _weight;

	/**
	 * Instantiates a new condition player weight.
	 * @param weight the weight
	 */
	public ConditionTargetWeight(int weight)
	{
		_weight = weight;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2Character targetObj = env.getTarget();
		if(targetObj != null && targetObj.isPlayer())
		{
			L2PcInstance target = targetObj.getActingPlayer();
			if(!target.getDietMode() && target.getMaxLoad() > 0)
			{
				int weightproc = (target.getCurrentLoad() - target.getBonusWeightPenalty()) * 100 / target.getMaxLoad();
				return weightproc < _weight;
			}
		}
		return false;
	}
}
