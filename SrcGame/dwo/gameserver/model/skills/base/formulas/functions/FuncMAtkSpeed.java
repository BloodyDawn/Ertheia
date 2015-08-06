package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:39
 */
public class FuncMAtkSpeed extends Func
{
	static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

	private FuncMAtkSpeed()
	{
		super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fas_instance;
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() * BaseStats.WIT.calcBonus(env.getCharacter()) * BaseStats.CHA.calcBonus(env.getCharacter()));
	}
}
