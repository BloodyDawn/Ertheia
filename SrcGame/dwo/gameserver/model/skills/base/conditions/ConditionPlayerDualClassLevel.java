package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.03.13
 * Time: 17:02
 */

public class ConditionPlayerDualClassLevel extends Condition
{
	private final int _level;

	public ConditionPlayerDualClassLevel(int level)
	{
		_level = level;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!env.getCharacter().isPlayer())
		{
			return false;
		}
		SubClass checkForDual = env.getCharacter().getActingPlayer().getDualSubclass();
		return checkForDual != null && checkForDual.getLevel() >= _level;
	}
}
