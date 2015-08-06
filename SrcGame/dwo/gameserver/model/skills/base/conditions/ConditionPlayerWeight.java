package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionPlayerWeight.
 *
 * @author Kerberos
 */
public class ConditionPlayerWeight extends Condition
{

	private final int _weight;

	/**
	 * Instantiates a new condition player weight.
	 *
	 * @param weight the weight
	 */
	public ConditionPlayerWeight(int weight)
	{
		_weight = weight;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getCharacter() instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) env.getCharacter();
			if(player.getMaxLoad() > 0)
			{
				int weightproc = (player.getCurrentLoad() - player.getBonusWeightPenalty()) * 100 / player.getMaxLoad();
				return weightproc < _weight || player.getDietMode();
			}
		}
		return true;
	}
}
