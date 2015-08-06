package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.stats.Env;

/**
 * @author mkizub
 */

public class LambdaCalc extends Lambda
{
	public Func[] funcs;

	public LambdaCalc()
	{
		funcs = new Func[0];
	}

	@Override
	public double calc(Env env)
	{
		double saveValue = env.getValue();
		try
		{
			env.setValue(0);
			for(Func f : funcs)
			{
				f.calc(env);
			}
			return env.getValue();
		}
		finally
		{
			env.setValue(saveValue);
		}
	}

	public void addFunc(Func f)
	{
		int len = funcs.length;
		Func[] tmp = new Func[len + 1];
		System.arraycopy(funcs, 0, tmp, 0, len);
		tmp[len] = f;
		funcs = tmp;
	}
}