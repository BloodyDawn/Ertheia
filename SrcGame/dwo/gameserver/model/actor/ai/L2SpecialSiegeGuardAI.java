package dwo.gameserver.model.actor.ai;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Character.AIAccessor;

import java.util.ArrayList;
import java.util.List;

public class L2SpecialSiegeGuardAI extends L2SiegeGuardAI
{
	private List<Integer> _allied;

	/**
	 * @param accessor
	 */
	public L2SpecialSiegeGuardAI(AIAccessor accessor)
	{
		super(accessor);
		_allied = new ArrayList<>();
	}

	public List<Integer> getAlly()
	{
		return _allied;
	}

	@Override
	protected boolean autoAttackCondition(L2Character target)
	{
		if(_allied.contains(target.getObjectId()))
		{
			return false;
		}

		return super.autoAttackCondition(target);
	}
}
