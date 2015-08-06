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
public class FuncBowAtkRange extends Func
{
	private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

	private FuncBowAtkRange()
	{
		super(Stats.POWER_ATTACK_RANGE, 0x10, null);
		setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
	}

	public static Func getInstance()
	{
		return _fbar_instance;
	}

	@Override
	public void calc(Env env)
	{
		if(!cond.test(env))
		{
			return;
		}
		// default is 40 and with bow should be 500
		env.setValue(env.getValue() + 460);
	}
}
