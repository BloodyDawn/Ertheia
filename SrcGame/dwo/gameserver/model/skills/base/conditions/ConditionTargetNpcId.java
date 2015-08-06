package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.stats.Env;

import java.util.List;

public class ConditionTargetNpcId extends Condition
{
	private final List<Integer> _npcIds;

	/**
	 * Instantiates a new condition target npc id.
	 * @param npcIds the npc ids
	 */
	public ConditionTargetNpcId(List<Integer> npcIds)
	{
		_npcIds = npcIds;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2Object target = env.getCharacter().getTarget();
		if(target == null)
		{
			return false;
		}

		if(target instanceof L2Npc)
		{
			return _npcIds.contains(((L2Npc) target).getNpcId());
		}

		if(target instanceof L2DoorInstance)
		{
			return _npcIds.contains(((L2DoorInstance) target).getDoorId());
		}

		return false;
	}
}
