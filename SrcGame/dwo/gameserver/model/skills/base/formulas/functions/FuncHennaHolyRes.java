package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:55
 */

public class FuncHennaHolyRes extends Func
{
	static final FuncHennaHolyRes _fh_instance = new FuncHennaHolyRes();

	private FuncHennaHolyRes()
	{
		super(Stats.HOLY_RES, 0x10, null);
	}

	public static Func getInstance()
	{
		return _fh_instance;
	}

	@Override
	public void calc(Env env)
	{
		L2PcInstance pc = (L2PcInstance) env.getCharacter();
		if(pc != null)
		{
			env.setValue(env.getValue());
		}
	}
}
