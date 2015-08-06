package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:56
 */

public class FuncMaxHpAdd extends Func
{
	static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();

	private FuncMaxHpAdd()
	{
		super(Stats.MAX_HP, 0x10, null);
	}

	public static Func getInstance()
	{
		return _fmha_instance;
	}

	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.getCharacter().getTemplate();
		env.setValue(env.getValue() + t.getBaseHp(env.getCharacter().getLevel()));
	}
}
