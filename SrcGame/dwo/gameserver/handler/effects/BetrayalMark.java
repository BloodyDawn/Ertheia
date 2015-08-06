package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class BetrayalMark extends L2Effect
{
	public BetrayalMark(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public BetrayalMark(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BETRAYALMARK;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2Playable)
		{
			L2Playable activeChar = (L2Playable) getEffected();
			activeChar.setIsUnderBetrayalMark(true);
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2Playable)
		{
			L2Playable activeChar = (L2Playable) getEffected();
			activeChar.setIsUnderBetrayalMark(false);
		}
	}

}
