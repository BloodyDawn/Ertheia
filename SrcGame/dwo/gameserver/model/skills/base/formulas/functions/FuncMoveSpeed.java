package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:36
 */
public class FuncMoveSpeed extends Func
{
	static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

	private FuncMoveSpeed()
	{
		super(Stats.RUN_SPEED, 0x30, null);
	}

	public static Func getInstance()
	{
		return _fms_instance;
	}

	@Override
	public void calc(Env env)
	{
		double base = env.getCharacter().calcStat(Stats.RUN_SPEED_MODIFY_DEX, 0, null, null);
		if(base > 0 && env.getCharacter().getDEX() > base)
		{
			env.addValue(env.getCharacter().getDEX() - base);
		}
	}
}
