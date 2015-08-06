package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;

import java.util.Map;

/**
 * @author Yorie
 */
@RestrictionCheckList({
	RestrictionCheck.DEAD, RestrictionCheck.PARTICIPATING_COMBAT, RestrictionCheck.BAD_REPUTATION,
	RestrictionCheck.PARTICIPATING_SIEGE, RestrictionCheck.CAN_MOVE, RestrictionCheck.CAN_CAST, RestrictionCheck.ONLINE,
	RestrictionCheck.MARRIED, RestrictionCheck.CASTING, RestrictionCheck.CASTING_SIMULTANEOUSLY,
	RestrictionCheck.IMMOBILIZED
})
public class StateController extends PlayerController implements IRestrictionChecker
{
	public StateController(L2PcInstance player)
	{
		super(player);
		player.getRestrictionController().addChecker(this);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		switch(check)
		{
			case DEAD:
				return player.isDead() || player.isAlikeDead();
			case PARTICIPATING_COMBAT:
				return player.isInCombat();
			case BAD_REPUTATION:
				return player.hasBadReputation();
			case PARTICIPATING_SIEGE:
				return player.isInSiege();
			case CAN_MOVE:
				return player.isMovementDisabled();
			case CAN_CAST:
				return player.isMuted();
			case ONLINE:
				return player.isOnline();
			case MARRIED:
				return player.isMarried();
			case IMMOBILIZED:
				return player.isImmobilized();
			case CASTING:
				return player.isCastingNow();
			case CASTING_SIMULTANEOUSLY:
				return player.isCastingSimultaneouslyNow();
		}
		return true;
	}

	/***
	 * @return {@code true} если игрок в данный момен может выполнить социальное действие
	 */
	public boolean canMakeSocialAction()
	{
		return player.getActiveRequester() == null || player.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE || player.getRestrictionController().check(RestrictionChain.CAN_MAKE_SOCIAL_ACTIONS).passed();

	}
}
