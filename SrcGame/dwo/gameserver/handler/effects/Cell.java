package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 04.12.11
 * Time: 9:59
 */

public class Cell extends L2Effect
{
	public Cell(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PETRIFICATION;
	}

	@Override
	public boolean onStart()
	{
		getEffected().startParalyze();
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		getEffected().stopParalyze(false);
		super.onExit();
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_PARALYZED | CharEffectList.EFFECT_FLAG_INVUL;
	}
}
