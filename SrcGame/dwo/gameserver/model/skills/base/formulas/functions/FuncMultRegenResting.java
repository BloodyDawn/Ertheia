package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.player.base.PlayerState;
import dwo.gameserver.model.skills.base.conditions.ConditionPlayerState;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:21
 */
public class FuncMultRegenResting extends Func
{
	static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

	/**
	 * Constructor of the FuncMultRegenResting.<BR>
	 * <BR>
	 * @param pStat
	 */
	private FuncMultRegenResting(Stats pStat)
	{
		super(pStat, 0x20, null);
		setCondition(new ConditionPlayerState(PlayerState.RESTING, true));
	}

	/**
	 * Return the Func object corresponding to the state concerned.<BR>
	 * <BR>
	 * @param stat
	 */
	public static Func getInstance(Stats stat)
	{
		int pos = stat.ordinal();

		if(_instancies[pos] == null)
		{
			_instancies[pos] = new FuncMultRegenResting(stat);
		}

		return _instancies[pos];
	}

	/**
	 * Calculate the modifier of the state concerned.<BR>
	 * <BR>
	 */
	@Override
	public void calc(Env env)
	{
		if(!cond.test(env))
		{
			return;
		}

		env.setValue(env.getValue() * 1.45);
	}
}
