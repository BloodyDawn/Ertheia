package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

import java.util.List;

public class ConditionTargetClassIdRestriction extends Condition
{
	private final List<Integer> _classIds;

	/**
	 * Instantiates a new condition target class id restriction.
	 *
	 * @param classId the class id
	 */
	public ConditionTargetClassIdRestriction(List<Integer> classId)
	{
		_classIds = classId;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getTarget() instanceof L2PcInstance))
		{
			return false;
		}
		return _classIds.contains(((L2PcInstance) env.getTarget()).getClassId().getId());
	}
}
