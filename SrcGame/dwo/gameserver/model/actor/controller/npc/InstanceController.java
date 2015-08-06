package dwo.gameserver.model.actor.controller.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;
import dwo.gameserver.model.world.Instance;

/**
 * Player instance controller.
 *
 * @author Yorie
 */
@RestrictionCheckList({
	RestrictionCheck.PARTICIPATING_INSTANCE, RestrictionCheck.CAN_SUMMON_TO_INSTANCE,
	RestrictionCheck.PARTICIPATING_SAME_INSTANCE
})
public class InstanceController extends dwo.gameserver.model.actor.controller.object.InstanceController
{
	public InstanceController(L2Npc object)
	{
		super(object);
	}

	/**
	 * Implements special NPC checks before moving between instances.
	 * @param oldInstance In what instance character now.
	 * @param newInstance To what instance character should be moved.
	 */
	@Override
	protected void prepareMoveInstance(Instance oldInstance, Instance newInstance)
	{
		if(isInInstance() && oldInstance != null)
		{
			oldInstance.removeNpc((L2Npc) object);
		}

		if(newInstance.getId() > 0)
		{
			newInstance.addNpc((L2Npc) object);
		}
	}
}
