package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:35
 */
public class FuncPAtkCritical extends Func
{
	static final FuncPAtkCritical _fac_instance = new FuncPAtkCritical();

	private FuncPAtkCritical()
	{
		super(Stats.PCRITICAL_RATE, 0x09, null);
	}

	public static Func getInstance()
	{
		return _fac_instance;
	}

	@Override
	public void calc(Env env)
	{
		env.mulValue(BaseStats.DEX.calcBonus(env.getCharacter()));
		if(env.getCharacter().getActiveWeaponInstance() != null || env.getCharacter() instanceof L2Summon)
		{
			env.mulValue(10);
		}

		env.setBaseValue(env.getValue());

		double base = env.getCharacter().calcStat(Stats.PCRITICAL_RATE_MODIFY_DEX, 0, null, null);
		if(base > 0 && env.getCharacter().getDEX() > base)
		{
			env.addValue(env.getCharacter().getDEX() - base);
		}
	}
}
