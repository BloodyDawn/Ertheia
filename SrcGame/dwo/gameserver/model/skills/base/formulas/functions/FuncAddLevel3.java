package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:03
 */

public class FuncAddLevel3 extends Func
{
	static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

	private FuncAddLevel3(Stats pStat)
	{
		super(pStat, 0x10, null);
	}

	public static Func getInstance(Stats stat)
	{
		int pos = stat.ordinal();
		if(_instancies[pos] == null)
		{
			_instancies[pos] = new FuncAddLevel3(stat);
		}
		return _instancies[pos];
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() + env.getCharacter().getLevel() / 3.0);
	}
}
