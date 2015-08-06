package dwo.gameserver.model.actor.restriction;

import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;
import java.util.Map;

/**
 * @author Yorie
 */
public class RestrictionManager
{
	private Map<RestrictionCheck, List<IRestrictionChecker>> checks = new FastMap<>();

	public static RestrictionManager getInstance()
	{
		return SingletonHolder.instance;
	}

	public void addChecker(IRestrictionChecker checker)
	{
		if(checker.getClass().isAnnotationPresent(RestrictionCheckList.class))
		{
			for(RestrictionCheck check : checker.getClass().getAnnotation(RestrictionCheckList.class).value())
			{
				addCheck(check, checker);
			}
		}
	}

	private void addCheck(RestrictionCheck check, IRestrictionChecker checker)
	{
		if(!checks.containsKey(check))
		{
			checks.put(check, new FastList<>());
		}

		if(!checks.get(check).contains(checker))
		{
			checks.get(check).add(checker);
		}
	}

	private void addChecks(RestrictionCheck[] checks, IRestrictionChecker checker)
	{
		for(RestrictionCheck check : checks)
		{
			addCheck(check, checker);
		}
	}

	public RestrictionResponse check(RestrictionChain action)
	{
		return check(action, new FastMap<>());
	}

	public RestrictionResponse check(RestrictionChain action, Map<RestrictionCheck, Object> params)
	{
		for(RestrictionCheck check : action.getPositiveChecks())
		{
			if(!checks.containsKey(check))
			{
				continue;
			}

			for(IRestrictionChecker checker : checks.get(check))
			{
				if(!checker.checkRestriction(check, params))
				{
					return new RestrictionResponse(checker.getClass(), check);
				}
			}
		}

		for(RestrictionCheck check : action.getNegativeChecks())
		{
			if(!checks.containsKey(check))
			{
				continue;
			}

			for(IRestrictionChecker checker : checks.get(check))
			{
				if(checker.checkRestriction(check, params))
				{
					return new RestrictionResponse(checker.getClass(), check);
				}
			}
		}

		return RestrictionResponse.DEFAULT;
	}

	private static class SingletonHolder
	{
		protected static final RestrictionManager instance = new RestrictionManager();
	}
}
