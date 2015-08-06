package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:37
 */
public class FuncPAtkSpeed extends Func
{
	static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

	private FuncPAtkSpeed()
	{
		super(Stats.POWER_ATTACK_SPEED, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fas_instance;
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() * BaseStats.DEX.calcBonus(env.getCharacter()) * BaseStats.CHA.calcBonus(env.getCharacter()));
	}
}
