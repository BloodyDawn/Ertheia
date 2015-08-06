package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:56
 */

public class FuncMaxHpMul extends Func
{
	static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

	private FuncMaxHpMul()
	{
		super(Stats.MAX_HP, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fmhm_instance;
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() * BaseStats.CON.calcBonus(env.getCharacter()) * BaseStats.CHA.calcBonus(env.getCharacter()));
	}
}
