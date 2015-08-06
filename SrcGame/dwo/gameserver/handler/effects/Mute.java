package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class Mute extends L2Effect
{
	public Mute(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MUTE;
	}

	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.MUTED);
		getEffected().startMuted();
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.MUTED);
		getEffected().stopMuted(false);
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_MUTED;
	}
}
