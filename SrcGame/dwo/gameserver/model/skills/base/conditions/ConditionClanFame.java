package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 19.03.12
 * Time: 9:27
 */

public class ConditionClanFame extends Condition
{
	private final int _reputation;

	public ConditionClanFame(int reputation)
	{
		_reputation = reputation;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getCharacter().getActingPlayer().getClan() == null)
		{
			return false;
		}
		if(!env.getCharacter().getActingPlayer().isClanLeader()) // TODO: Временно
		{
			return false;
		}
		return env.getCharacter().getActingPlayer().getClan().getReputationScore() >= _reputation;
	}
}
