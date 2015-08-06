package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:35
 */

public class FuncMAtkCritical extends Func
{
	static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

	private FuncMAtkCritical()
	{
		super(Stats.MCRITICAL_RATE, 0x09, null);
	}

	public static Func getInstance()
	{
		return _fac_instance;
	}

	@Override
	public void calc(Env env)
	{
		L2Character p = env.getCharacter();
		// CT2: The magic critical rate has been increased to 50 times.
		if(p.isPlayer())
		{
			env.mulValue(BaseStats.WIT.calcBonus(p));
		}
		else
		{
			env.mulValue(BaseStats.WIT.calcBonus(p) * 10.0);
		}
	}
}
