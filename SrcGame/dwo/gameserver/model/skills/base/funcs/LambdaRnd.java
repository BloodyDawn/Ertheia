package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.Rnd;

/**
 * @author mkizub
 */

public class LambdaRnd extends Lambda
{
	private final Lambda _max;
	private final boolean _linear;

	public LambdaRnd(Lambda max, boolean linear)
	{
		_max = max;
		_linear = linear;
	}

	@Override
	public double calc(Env env)
	{
		if(_linear)
		{
			return _max.calc(env) * Rnd.nextDouble();
		}
		return _max.calc(env) * Rnd.nextGaussian();
	}
}
