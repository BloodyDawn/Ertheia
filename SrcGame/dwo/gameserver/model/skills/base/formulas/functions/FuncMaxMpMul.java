package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:59
 */

public class FuncMaxMpMul extends Func
{
	static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

	private FuncMaxMpMul()
	{
		super(Stats.MAX_MP, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fmmm_instance;
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() * BaseStats.MEN.calcBonus(env.getCharacter()) * BaseStats.CHA.calcBonus(env.getCharacter()));
	}
}
