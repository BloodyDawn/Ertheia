package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:24
 */
public class FuncPAtkMod extends Func
{
	static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

	private FuncPAtkMod()
	{
		super(Stats.POWER_ATTACK, 0x30, null);
	}

	public static Func getInstance()
	{
		return _fpa_instance;
	}

	@Override
	public void calc(Env env)
	{
		if(env.getCharacter().isPlayer())
		{
			env.mulValue(BaseStats.STR.calcBonus(env.getPlayer()) * env.getPlayer().getLevelMod() * BaseStats.CHA.calcBonus(env.getPlayer()));
		}
		else
		{
			env.mulValue(BaseStats.STR.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod() * BaseStats.CHA.calcBonus(env.getCharacter()));
		}
	}
}
