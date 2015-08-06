package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:57
 */

public class FuncMaxCpAdd extends Func
{
	static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();

	private FuncMaxCpAdd()
	{
		super(Stats.MAX_CP, 0x10, null);
	}

	public static Func getInstance()
	{
		return _fmca_instance;
	}

	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.getCharacter().getTemplate();
		env.setValue(env.getValue() + t.getBaseCp(env.getCharacter().getLevel()));
	}
}
