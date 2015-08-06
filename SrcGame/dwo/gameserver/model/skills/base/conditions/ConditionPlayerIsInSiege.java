package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 10.05.12
 * Time: 1:23
 * Кондишн, определяющий игрок в осадной зоне или нет.
 */

public class ConditionPlayerIsInSiege extends Condition
{
	private final boolean _val;

	public ConditionPlayerIsInSiege(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().isInsideZone(L2Character.ZONE_SIEGE) == _val;
	}
}
