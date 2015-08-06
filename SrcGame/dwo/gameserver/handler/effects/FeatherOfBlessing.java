package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class FeatherOfBlessing extends L2Effect
{
	public FeatherOfBlessing(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DEBUFF;
	}

	@Override
	public boolean onStart()
	{
		return true;
	}

	@Override
	public void onExit()
	{

	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_FEATHER_OF_BLESSING;
	}
}
