package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.stats.Env;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The Class ConditionTargetRace.
 *
 * @author mkizub
 */
public class ConditionTargetRace extends Condition
{
	private final Race[] _races;

	/**
	 * Instantiates a new condition target race.
	 * @param races the list containing the allowed races.
	 */
	public ConditionTargetRace(Race[] races)
	{
		_races = races;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getTarget() instanceof L2PcInstance))
		{
			return false;
		}
		return ArrayUtils.contains(_races, env.getTarget().getActingPlayer().getRace());
	}
}
