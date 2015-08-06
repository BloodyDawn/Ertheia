package dwo.gameserver.model.actor.controller.object;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionManager;
import dwo.gameserver.model.actor.restriction.RestrictionResponse;

import java.util.Map;

/**
 * @author Yorie
 */
public class RestrictionController extends L2ObjectController
{
	private final RestrictionManager manager = new RestrictionManager();

	public RestrictionController(L2Object player)
	{
		super(player);
	}

	public void addChecker(IRestrictionChecker checker)
	{
		manager.addChecker(checker);
	}

	public RestrictionResponse check(RestrictionChain action, Map<RestrictionCheck, Object> params)
	{
		return manager.check(action, params);
	}

	public RestrictionResponse check(RestrictionChain type)
	{
		return manager.check(type);
	}
}
