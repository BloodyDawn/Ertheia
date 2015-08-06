package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class Sleep extends L2Effect
{
	public Sleep(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SLEEP;
	}

	@Override
	public boolean onStart()
	{
		addRemovedEffectType(L2EffectStopCond.ON_DAMAGE_DEBUFF);
		getEffected().startAbnormalEffect(AbnormalEffect.SLEEP);
		getEffected().startSleeping();
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.SLEEP);
		getEffected().stopSleeping(false);
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_SLEEP;
	}
}
