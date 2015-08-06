package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
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
	private final L2PcInstance player;

	public InstanceController(L2Object object)
	{
		super(object);
		player = (L2PcInstance) object;
	}

	@Override
	protected void prepareMoveInstance(Instance oldInstance, Instance newInstance)
	{
		if(isInInstance() && oldInstance != null)
		{
			oldInstance.removePlayer(object.getObjectId());
		}

		if(newInstance.getId() > 0)
		{
			newInstance.addPlayer(object.getObjectId());
		}

		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				pet.getInstanceController().setInstanceId(newInstance.getId());
			}
		}

		HookManager.getInstance().notifyEvent(HookType.ON_ENTER_INSTANCE, player.getHookContainer(), player, newInstance);
	}
}
