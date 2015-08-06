package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.skills.base.conditions.Condition;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;

public class FuncTemplate
{
	protected static final Logger _log = LogManager.getLogger(FuncTemplate.class);
	public final Class<?> func;
	public final Constructor<?> constructor;
	public final Stats stat;
	public final int order;
	public final Lambda lambda;
	public Condition attachCond;
	public Condition applayCond;

	public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		stat = pStat;
		order = pOrder;
		lambda = pLambda;
		try
		{
			func = Class.forName("dwo.gameserver.model.skills.base.funcs.Func" + pFunc);
		}
		catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			constructor = func.getConstructor(// Stats to update
				Stats.class,
				// Order of execution
				Integer.TYPE,
				// Owner
				Object.class,
				// Value for function
				Lambda.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Func getFunc(Env env, Object owner)
	{
		if(attachCond != null && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			Func f = (Func) constructor.newInstance(stat, order, owner, lambda);
			if(applayCond != null)
			{
				f.setCondition(applayCond);
			}
			return f;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
			return null;
		}
	}
}