package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.base.conditions.ConditionUsingItemType;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * User: Keiichi
 * Date: 11.08.12
 * Time: 1:17
 * L2GOD Team/
 */
public class FuncTwoHandCrossBowAtkRange extends Func
{
	private static final FuncTwoHandCrossBowAtkRange _fcb_instance = new FuncTwoHandCrossBowAtkRange();

	private FuncTwoHandCrossBowAtkRange()
	{
		super(Stats.POWER_ATTACK_RANGE, 0x10, null);
		setCondition(new ConditionUsingItemType(L2WeaponType.TWOHANDCROSSBOW.mask()));
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
