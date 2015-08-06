package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.base.conditions.ConditionUsingItemType;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:31
 */
public class FuncCrossBowAtkRange extends Func
{
	private static final FuncCrossBowAtkRange _fcb_instance = new FuncCrossBowAtkRange();

	private FuncCrossBowAtkRange()
	{
		super(Stats.POWER_ATTACK_RANGE, 0x10, null);
		setCondition(new ConditionUsingItemType(L2WeaponType.CROSSBOW.mask()));
	}

	public static Func getInstance()
	{
		return _fcb_instance;
	}

	@Override
	public void calc(Env env)
	{
		if(!cond.test(env))
		{
			return;
		}
		// default is 40 and with crossbow should be 400
		env.setValue(env.getValue() + 360);
	}
}
