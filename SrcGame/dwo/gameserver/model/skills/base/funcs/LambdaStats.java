package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.stats.Env;

/**
 * @author mkizub
 */

public class LambdaStats extends Lambda
{
	private final StatsType _stat;

	public LambdaStats(StatsType stat)
	{
		_stat = stat;
	}

	@Override
	public double calc(Env env)
	{
		switch(_stat)
		{
			case PLAYER_LEVEL:
				if(env.getCharacter() == null)
				{
					return 1;
				}
				return env.getCharacter().getLevel();
			case CUBIC_LEVEL:
				if(env.getCubic() == null)
				{
					return 1;
				}
				return env.getCubic().getOwner().getLevel();
			case TARGET_LEVEL:
				if(env.getTarget() == null)
				{
					return 1;
				}
				return env.getTarget().getLevel();
			case PLAYER_MAX_HP:
				if(env.getCharacter() == null)
				{
					return 1;
				}
				return env.getCharacter().getMaxHp();
			case PLAYER_MAX_MP:
				if(env.getCharacter() == null)
				{
					return 1;
				}
				return env.getCharacter().getMaxMp();
		}
		return 0;
	}

	public enum StatsType
	{
		PLAYER_LEVEL,
		CUBIC_LEVEL,
		TARGET_LEVEL,
		PLAYER_MAX_HP,
		PLAYER_MAX_MP
	}
}