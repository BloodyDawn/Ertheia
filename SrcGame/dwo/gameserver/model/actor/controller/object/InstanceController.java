package dwo.gameserver.model.actor.controller.object;

import dwo.config.Config;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;
import dwo.gameserver.model.world.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Player instance controller.
 *
 * @author Yorie
 */
@RestrictionCheckList({
	RestrictionCheck.PARTICIPATING_INSTANCE, RestrictionCheck.CAN_SUMMON_TO_INSTANCE,
	RestrictionCheck.PARTICIPATING_SAME_INSTANCE
})
public class InstanceController extends L2ObjectController implements IRestrictionChecker
{
	protected int instanceId;

	public InstanceController(L2Object object)
	{
		super(object);
		object.getRestrictionController().addChecker(this);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		switch(check)
		{
			case PARTICIPATING_INSTANCE:
				return isInInstance();
			case CAN_SUMMON_TO_INSTANCE:
				return canSummonToInstance();
			case PARTICIPATING_SAME_INSTANCE:
				return params == null || !params.containsKey(RestrictionCheck.PARTICIPATING_SAME_INSTANCE) || ((SameInstanceCheckEntity) params.get(RestrictionCheck.PARTICIPATING_SAME_INSTANCE)).getTarget().getInstanceId() == instanceId;
		}
		return true;
	}

	/**
	 * Checks whether player can do summon playable objects to its instance.
	 * @return True if summon is allowed.
	 */
	public boolean canSummonToInstance()
	{
		Instance instance = getActiveInstance();
		return instance != null && instance.isSummonAllowed() && Config.ALLOW_SUMMON_TO_INSTANCE;
	}

	/**
	 * Returns active player instance if it exists.
	 * @return Active world.
	 */
	@Nullable
	public Instance getActiveInstance()
	{
		return InstanceManager.getInstance().getInstance(instanceId);
	}

	/**
	 * Check if player in some custom instance instead of main world or multiverse.
	 * @return True if player participating custom instance.
	 */
	public boolean isInInstance()
	{
		return instanceId > 0;
	}

	/**
	 * Checks if player is in multiverse.
	 * Multiverse is parallel universe of global L2 world.
	 * So, players in multiverse are not accessible for players in main world and vice versa, even if they in same location.
	 * @return True if player is in multiverse.
	 */
	public boolean isInMultiverse()
	{
		return instanceId == -1;
	}

	/**
	 * Fetches current player instance.
	 * @return Instance.
	 */
	@Nullable
	public Instance getInstance()
	{
		return InstanceManager.getInstance().getInstance(instanceId);
	}

	/**
	 * @return Current instance ID.
	 */
	public int getInstanceId()
	{
		return instanceId;
	}

	/**
	 * @param instanceId The id of the instance zone the object is in - id 0 is global
	 */
	public void setInstanceId(int instanceId)
	{
		if(this.instanceId == instanceId)
		{
			return;
		}

		Instance oldInstance = getInstance();
		Instance newInstance = InstanceManager.getInstance().getInstance(instanceId);

		if(newInstance == null)
		{
			return;
		}

		prepareMoveInstance(oldInstance, newInstance);

		this.instanceId = instanceId;

		// If we change it for visible objects, we must clear & re-validate known lists
		if(object.getLocationController().isVisible() && object.getKnownList() != null)
		{
			object.getLocationController().decay();
			object.getLocationController().spawn();
		}
	}

	/**
	 * Preparation state of movement object to instance.
	 * @param oldInstance In what instance character now.
	 * @param newInstance To what instance character should be moved.
	 */
	protected void prepareMoveInstance(Instance oldInstance, Instance newInstance)
	{
		if(object instanceof L2Npc)
		{
			if(instanceId > 0 && oldInstance != null)
			{
				oldInstance.removeNpc((L2Npc) object);
			}
			if(instanceId > 0)
			{
				newInstance.addNpc((L2Npc) object);
			}
		}
	}

	/**
	 * Container for checking PARTICIPATING_SAME_INSTANCE restriction check.
	 *
	 * @author Yorie
	 */
	public static class SameInstanceCheckEntity
	{
		private final L2Object target;

		public SameInstanceCheckEntity(L2Object target)
		{
			this.target = target;
		}

		public L2Object getTarget()
		{
			return target;
		}
	}
}
