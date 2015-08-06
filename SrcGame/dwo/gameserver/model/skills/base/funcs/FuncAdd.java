package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

public class FuncAdd extends Func
{
	private final Lambda _lambda;

	public FuncAdd(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
		_lambda = lambda;
	}

	public Lambda getLambda()
	{
		return _lambda;
	}

	@Override
	public void calc(Env env)
	{
		if(cond == null || cond.test(env))
		{
			env.addValue(_lambda.calc(env));
		}
	}
}
