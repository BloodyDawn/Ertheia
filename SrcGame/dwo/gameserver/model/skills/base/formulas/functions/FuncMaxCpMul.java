package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:58
 */

public class FuncMaxCpMul extends Func
{
	static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

	private FuncMaxCpMul()
	{
		super(Stats.MAX_CP, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fmcm_instance;
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() * BaseStats.CON.calcBonus(env.getCharacter()) * BaseStats.CHA.calcBonus(env.getCharacter()));
	}
}
