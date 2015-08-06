package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 31.01.12
 * Time: 21:07
 */

public class ConditionPlayerSummonPoints extends Condition
{
	private final int _points;

	public ConditionPlayerSummonPoints(int points)
	{
		_points = points;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = env.getCharacter().getActingPlayer();

		return player.getMaxSummonPoints() - player.getUsedSummonPoints() >= _points;
	}
}
