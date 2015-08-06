package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 27.08.12
 * Time: 14:22
 */

public class CancelAllDebuff extends L2Effect
{
	public CancelAllDebuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL_DEBUFF;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		for(L2Effect effect : getEffected().getAllEffects())
		{
			if(effect == null)
			{
				continue;
			}

			if((effect.getSkill().isDebuff() || effect.getEffectType() == L2EffectType.DEBUFF) && effect.getSkill().canBeDispeled())
			{
				effect.cancel();
			}
		}
		return true;
	}

}
