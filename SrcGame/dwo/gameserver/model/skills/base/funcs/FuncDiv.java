package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

public class FuncDiv extends Func
{
	private final Lambda _lambda;

	public FuncDiv(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
		_lambda = lambda;
	}

	@Override
	public void calc(Env env)
	{
		if(cond == null || cond.test(env))
		{
			env.divValue(_lambda.calc(env));
		}
	}
}