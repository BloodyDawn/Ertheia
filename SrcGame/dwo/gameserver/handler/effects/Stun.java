package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class Stun extends L2Effect
{
	public Stun(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.STUN;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected().isFlying())
		{
			getEffected().startAbnormalEffect(AbnormalEffect.S_AIR_STUN);
		}
		else
		{
			getEffected().startAbnormalEffect(AbnormalEffect.STUN);
		}
		getEffected().startStunning();
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.STUN);
		getEffected().stopAbnormalEffect(AbnormalEffect.S_AIR_STUN);
		getEffected().stopStunning(false);
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_STUNNED;
	}
}
