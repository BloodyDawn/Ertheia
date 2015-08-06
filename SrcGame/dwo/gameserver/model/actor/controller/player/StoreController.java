package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;

import java.util.Map;

/**
 * @author Yorie
 */
@RestrictionCheckList(RestrictionCheck.TRADING)
public class StoreController extends PlayerController implements IRestrictionChecker
{
	public StoreController(L2PcInstance player)
	{
		super(player);
		player.getRestrictionController().addChecker(this);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		return player.isInStoreMode();
	}
}
