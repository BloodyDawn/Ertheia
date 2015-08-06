package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.02.12
 * Time: 12:29
 */

public class IgnoreDeath extends L2Effect
{
	public IgnoreDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		getEffected().setIsMortal(false);
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().setIsMortal(true);
	}
}
