package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;
import dwo.gameserver.model.player.formation.group.PartyExitReason;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.ObserverEnd;
import dwo.gameserver.network.game.serverpackets.ObserverStart;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseObserveMode;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadMode;

import java.util.Map;

/**
 * Observer mode controller.
 *
 * @author Yorie
 */
@RestrictionCheckList(RestrictionCheck.OBSERVING)
public class ObserverController extends PlayerController implements IRestrictionChecker
{
	private boolean isObserving;
	private ObserveType type;

	public ObserverController(L2PcInstance player)
	{
		super(player);
		player.getRestrictionController().addChecker(this);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		return isObserving;
	}

	/**
	 * @return True if current player is in observer mode.
	 */
	public boolean isObserving()
	{
		return isObserving;
	}

	/**
	 * @return True if current player observing olympiad game.
	 */
	public boolean isOlympiadObserver()
	{
		return isObserving && type == ObserveType.OLYMPIAD;
	}

	/**
	 * Enters in normal observer mode.
	 * @param loc Location to start observing.
	 */
	public void enter(Location loc)
	{
		enter(loc, ObserveType.NORMAL, 0);
	}

	/**
	 * Enters observe mode depending of its type and instance ID.
	 * @param loc Location to start observing.
	 * @param type Type of observation.
	 * @param instanceId ID of instance that player should observe.
	 */
	public void enter(Location loc, ObserveType type, int instanceId)
	{
		this.type = type;
		enterObserverMode(loc, type, instanceId);
	}

	/**
	 * Enters in observer mode starting from given location.
	 */
	private void enterObserverMode(Location location, ObserveType type, int instanceId)
	{
		player.stopAllEffectsExceptThoseThatLastThroughDeath();

		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				pet.getLocationController().decay();
			}
		}

		if(!player.getCubics().isEmpty())
		{
			for(L2CubicInstance cubic : player.getCubics())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}

			player.getCubics().clear();
		}

		if(player.getParty() != null)
		{
			player.getParty().removePartyMember(player, PartyExitReason.EXPELLED);
		}

		if(player.isSitting())
		{
			player.standUp();
		}

		if(!isObserving)
		{
			player.getLocationController().rememberLocation();
		}

		isObserving = true;
		player.setTarget(null);
		player.setIsInvul(true);
		player.getAppearance().setInvisible();

		switch(type)
		{
			case NORMAL:
				player.sendPacket(new ObserverStart(location.getX(), location.getY(), location.getZ()));
				player.setXYZ(location.getX(), location.getY(), location.getZ());
				break;
			case CHAOS_FESTIVAL:
				player.sendPacket(new ExCuriousHouseObserveMode(true));
				player.teleToInstance(location, instanceId);
				break;
			case OLYMPIAD:
				player.teleToLocation(location, false);
				player.getInstanceController().setInstanceId(OlympiadGameManager.getInstance().getOlympiadTask(instanceId).getZone().getInstanceId());
				player.sendPacket(new ExOlympiadMode(3));
				break;
		}

		player.broadcastUserInfo();
		this.type = type;
	}

	/**
	 * Leaves current observer mode.
	 */
	public void leave()
	{
		if(isObserving)
		{
			leaveObserverMode();
		}

		type = null;
	}

	/**
	 * Leaves observer mode.
	 */
	private void leaveObserverMode()
	{
		if(type == ObserveType.OLYMPIAD && player.getOlympiadController().getGameId() == -1)
		{
			return;
		}

		player.setFalling(); // prevent receive falling damage
		isObserving = false;
		player.setTarget(null);

		switch(type)
		{
			case NORMAL:
				player.sendPacket(new ObserverEnd(player));
				break;
			case CHAOS_FESTIVAL:
				player.sendPacket(new ExCuriousHouseObserveMode(false));
				break;
			case OLYMPIAD:
				player.getOlympiadController().setGameId(-1);
				player.sendPacket(new ExOlympiadMode(0));
				break;
		}

		player.getInstanceController().setInstanceId(0);

		player.getLocationController().teleportToMemorizedLocation();
		player.getLocationController().forgetLocation();

		if(!player.isGM())
		{
			player.getAppearance().setVisible();
			player.setIsInvul(false);
		}

		if(player.hasAI())
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}

		player.broadcastUserInfo();
	}

	public static enum ObserveType
	{
		NORMAL,
		OLYMPIAD,
		CHAOS_FESTIVAL
	}
}
