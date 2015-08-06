package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:32
 */
public class FuncPAtkAccuracy extends Func
{
	static final FuncPAtkAccuracy instance = new FuncPAtkAccuracy();

	private FuncPAtkAccuracy()
	{
		super(Stats.ACCURACY_PHYSICAL, 0x10, null);
	}

	public static Func getInstance()
	{
		return instance;
	}

	@Override
	public void calc(Env env)
	{
		int level = env.getCharacter().getLevel();
		env.setValue(Math.sqrt(env.getCharacter().getDEX()) * 5 + level);

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

		if(env.getCharacter() instanceof L2Summon)
		{
			env.addValue(level < 60 ? 4 : 5);
		}

		// Поправка
		env.addValue(0.001);

	}
}
