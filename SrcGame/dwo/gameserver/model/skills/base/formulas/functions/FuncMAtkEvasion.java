package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 06.11.11
 * Time: 13:33
 */

public class FuncMAtkEvasion extends Func
{
	static final FuncMAtkEvasion instance = new FuncMAtkEvasion();

	private FuncMAtkEvasion()
	{
		super(Stats.EVASION_MAGICAL_RATE, 0x10, null);
	}

	public static Func getInstance()
	{
		return instance;
	}

	@Override
	public void calc(Env env)
	{
        env.setValue(env.getCharacter().getLevel() * 2 + Math.sqrt(env.getCharacter().getWIT()) * 3.);
	}
}
