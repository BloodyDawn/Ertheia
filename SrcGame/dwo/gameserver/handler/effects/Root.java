package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class Root extends L2Effect
{
	public Root(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ROOT;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected().isFlying())
		{
			getEffected().startAbnormalEffect(AbnormalEffect.S_AIR_ROOT);
		}
		else
		{
			getEffected().startAbnormalEffect(AbnormalEffect.ROOT);
		}
		getEffected().startRooted();
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.ROOT);
		getEffected().stopAbnormalEffect(AbnormalEffect.S_AIR_ROOT);
		getEffected().stopRooting(false);
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_ROOTED;
	}
}
