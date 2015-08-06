package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.stats.Env;

/**
 * @author mkizub
 */

public class LambdaConst extends Lambda
{
	private final double _value;

	public LambdaConst(double value)
	{
		_value = value;
	}

	@Override
	public double calc(Env env)
	{
		return _value;
	}
}
