package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:10
 */

public class FuncMultLevelMod extends Func
{
	static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

	private FuncMultLevelMod(Stats pStat)
	{
		super(pStat, 0x20, null);
	}

	public static Func getInstance(Stats stat)
	{
		int pos = stat.ordinal();
		if(_instancies[pos] == null)
		{
			_instancies[pos] = new FuncMultLevelMod(stat);
		}
		return _instancies[pos];
	}

	@Override
	public void calc(Env env)
	{
		env.setValue(env.getValue() * env.getCharacter().getLevelMod());
	}
}
