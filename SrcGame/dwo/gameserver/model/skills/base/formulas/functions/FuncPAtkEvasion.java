package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Corr: GenCloud
 * Date: 29.09.11
 * Time: 21:34
 * TODO: шанс уворота от атаки, возможно не точно! Math.sqrt(env.getCharacter().getLUC())
 */

public class FuncPAtkEvasion extends Func
{
	static final FuncPAtkEvasion _fae_instance = new FuncPAtkEvasion();

	private FuncPAtkEvasion()
	{
		super(Stats.EVASION_PHYSICAL_RATE, 0x10, null);
	}

	public static Func getInstance()
	{
		return _fae_instance;
	}

	@Override
	public void calc(Env env)
	{
		//  172 = dex(лвк) 55 level 99
		//  169 = dex(лвк) 55 level 99  - 3.75
		//  161 = dex(лвк) 27 level 99
		//  174	= dex(лвк) 61 level 99
		//  171	= dex(лвк) 61 level 99  - 3.75
		//	170	= dex(лвк) 50 level 99

		int level = env.getCharacter().getLevel();
		env.setValue(Math.sqrt(env.getCharacter().getDEX()) * Math.sqrt(env.getCharacter().getLUC()) * 5 + level);

		if(level > 69)
		{
			env.addValue(env.getCharacter().getLevel() - 69);
		}
		if(level > 77)
		{
			env.addValue(1);
		}
		if(level > 80)
		{
			env.addValue(2);
		}
		if(level > 87)
		{
			env.addValue(1);
		}
		if(level > 92)
		{
			env.addValue(1);
		}
		if(level > 97)
		{
			env.addValue(1);
		}

		// Поправка
		env.addValue(0.001);
	}
}
