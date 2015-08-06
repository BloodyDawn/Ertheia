package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class UnsummonAgathion extends SummonAgathion
{
	public UnsummonAgathion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onExit()
	{
		super.onExit();

		for(L2Effect effect : getEffected().getAllEffects())
		{
			if(effect == null)
			{
				continue;
			}

			if(effect.getEffectType() == L2EffectType.SUMMON_AGATHION)
			{
				effect.exit();
			}
		}
	}

}