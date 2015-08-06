package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:59
 */

public class FuncMaxMpAdd extends Func
{
	static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();

	private FuncMaxMpAdd()
	{
		super(Stats.MAX_MP, 0x10, null);
	}

	public static Func getInstance()
	{
		return _fmma_instance;
	}

	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.getCharacter().getTemplate();
		env.setValue(env.getValue() + t.getBaseMp(env.getCharacter().getLevel()));
	}
}
