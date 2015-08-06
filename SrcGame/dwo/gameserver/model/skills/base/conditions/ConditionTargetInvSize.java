package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

public class ConditionTargetInvSize extends Condition
{
	private final int _size;

	/**
	 * Instantiates a new condition player inv size.
	 * @param size the size
	 */
	public ConditionTargetInvSize(int size)
	{
		_size = size;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2Character targetObj = env.getTarget();
		if(targetObj != null && targetObj.isPlayer())
		{
			L2PcInstance target = targetObj.getActingPlayer();
			return target.getInventory().getSize(false) <= target.getInventoryLimit() - _size;
		}
		return false;
	}
}
