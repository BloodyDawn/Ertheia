package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.zone.TargetPosition;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 03.04.12
 * Time: 9:27
 */

public class ConditionTargetPosition extends Condition
{
	private final TargetPosition _position;

	public ConditionTargetPosition(TargetPosition position)
	{
		_position = position;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getTarget() == null)
		{
			return false;
		}

		return env.getCharacter().getTargetPosition(env.getTarget()) == _position;
	}
}
