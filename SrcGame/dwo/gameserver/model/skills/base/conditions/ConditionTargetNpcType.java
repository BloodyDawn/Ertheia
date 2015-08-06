package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.stats.Env;

import java.util.List;

public class ConditionTargetNpcType extends Condition
{
	private final List<Class<? extends L2Object>> _npcType;

	/**
	 * Instantiates a new condition target npc type.
	 * @param type the type
	 */
	public ConditionTargetNpcType(List<Class<? extends L2Object>> type)
	{
		_npcType = type;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getTarget() == null)
		{
			return false;
		}

		for(Class<? extends L2Object> type : _npcType)
		{
			if(env.getTarget().is(type))
			{
				return true;
			}
		}

		return false;
	}
}
