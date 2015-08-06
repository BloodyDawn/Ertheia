package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:26
 */

public class FuncMAtkMod extends Func
{
	static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

	private FuncMAtkMod()
	{
		super(Stats.MAGIC_ATTACK, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fma_instance;
	}

	@Override
	public void calc(Env env)
	{
		// Level Modifier^2 * INT Modifier^2
		double lvlMod = env.getCharacter().isPlayer() ? BaseStats.INT.calcBonus(env.getPlayer()) : BaseStats.INT.calcBonus(env.getCharacter());
        double chaMod = env.getCharacter().isPlayer() ? BaseStats.CHA.calcBonus(env.getPlayer()) : BaseStats.CHA.calcBonus(env.getCharacter());
		double intMod = env.getCharacter().isPlayer() ? env.getPlayer().getLevelMod() : env.getCharacter().getLevelMod();
		env.mulValue(Math.pow(lvlMod, 2) * Math.pow(intMod, 2) * Math.pow(chaMod, 2));
	}
}
