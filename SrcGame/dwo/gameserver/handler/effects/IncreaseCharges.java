package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author DS
 * Effect will generate charges for L2PcInstance targets
 * Number of charges in "value", maximum number in "count" effect variables
 */

public class IncreaseCharges extends L2Effect
{
	public IncreaseCharges(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.INCREASE_CHARGES;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() == null)
		{
			return false;
		}

		if(!(getEffected() instanceof L2PcInstance))
		{
			return false;
		}

		((L2PcInstance) getEffected()).increaseCharges((int) calc(), getCount());
		return true;
	}
}