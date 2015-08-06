package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;

public class ConditionPlayerMp extends Condition
{
	private final int _mp;

	/**
	 * Instantiates a new condition player mp.
	 *
	 * @param mp the mp
	 */
	public ConditionPlayerMp(int mp)
	{
		_mp = mp;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getCurrentMp() * 100 / env.getCharacter().getMaxMp() <= _mp;
	}
}
