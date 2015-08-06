package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author kerberos_20
 */

public class CharmOfLuck extends L2Effect
{
	public CharmOfLuck(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHARM_OF_LUCK;
	}

	@Override
	public boolean onStart()
	{
		return true;
	}

	@Override
	public void onExit()
	{
		((L2Playable) getEffected()).stopCharmOfLuck(this);
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_CHARM_OF_LUCK;
	}
}
