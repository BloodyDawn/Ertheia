package dwo.gameserver.model.actor.restriction;

import java.util.Map;

/**
 * @author Yorie
 */
public interface IRestrictionChecker
{
	boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params);
}
