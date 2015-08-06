package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 18.02.12
 * Time: 11:35
 */

public class ConditionTargetHp extends Condition
{

	private final int _hp;

	public ConditionTargetHp(int hp)
	{
		_hp = hp;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getTarget() == null)
		{
			return false;
		}

		if(!(env.getTarget() instanceof L2MonsterInstance))
		{
			return false;
		}

		return env.getTarget().getCurrentHp() * 100 / env.getTarget().getMaxVisibleHp() <= _hp;
	}
}